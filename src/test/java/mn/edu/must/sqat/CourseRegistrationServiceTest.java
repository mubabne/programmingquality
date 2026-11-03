package mn.edu.must.sqat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {

    private static final String STUDENT_ID = "S001";
    private static final String COURSE_ID = "F.CSA313";
    private static final String EMAIL = "student@example.com";
    private static final String SUBJECT = "Бүртгэл баталгаажлаа";

    @Mock
    private StudentRepository repository;

    @Mock
    private EmailSender emailSender;

    private CourseRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new CourseRegistrationService(repository, emailSender);
    }

    @Test
    void register_activeStudent_returnsOkAndSavesRegistrationAndSendsEmail() {
        stubSuccessfulRegistration();

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("OK", result);
        verify(repository).saveRegistration(STUDENT_ID, COURSE_ID);
        verify(emailSender).send(EMAIL, SUBJECT,
                COURSE_ID + " хичээлд амжилттай бүртгэгдлээ.");
    }

    @Test
    void register_missingStudent_returnsErrorWithoutSavingOrSendingEmail() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(null);

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("ERROR_NO_STUDENT", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void register_inactiveStudent_returnsErrorWithoutEmailInteractions() {
        Student student = new Student(STUDENT_ID, EMAIL, false, List.of("F.CSA101"));
        when(repository.findStudent(STUDENT_ID)).thenReturn(student);

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("ERROR_INACTIVE_STUDENT", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    @Test
    void register_missingPrerequisites_returnsErrorWithoutSaving() {
        Student student = new Student(STUDENT_ID, EMAIL, true, List.of("F.CSA101"));
        when(repository.findStudent(STUDENT_ID)).thenReturn(student);
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID))
                .thenReturn(List.of("F.CSA101", "F.CSA102"));

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("ERROR_PREREQUISITES", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    @Test
    void register_emailSenderThrows_returnsOkAndKeepsRegistration() {
        stubSuccessfulRegistration();
        doThrow(new IllegalStateException("SMTP unavailable"))
                .when(emailSender).send(eq(EMAIL), anyString(), anyString());

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("OK", result);
        verify(repository).saveRegistration(STUDENT_ID, COURSE_ID);
        verify(emailSender).send(eq(EMAIL), eq(SUBJECT), anyString());
    }

    @Test
    void register_nullArguments_throwsWithoutDependencyInteractions() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.register(null, COURSE_ID)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.register(STUDENT_ID, null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.register(null, null)));

        verifyNoInteractions(repository, emailSender);
    }

    @Test
    void register_successfulRegistration_emailBodyContainsCourseCode() {
        stubSuccessfulRegistration();
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        assertEquals("OK", service.register(STUDENT_ID, COURSE_ID));

        verify(repository).saveRegistration(STUDENT_ID, COURSE_ID);
        verify(emailSender).send(eq(EMAIL), eq(SUBJECT), bodyCaptor.capture());
        assertTrue(bodyCaptor.getValue().contains(COURSE_ID));
    }

    @ParameterizedTest(name = "{index}: {3}")
    @CsvSource({
            "false, false, false, ERROR_NO_STUDENT",
            "true, false, false, ERROR_INACTIVE_STUDENT",
            "true, true, false, ERROR_NO_COURSE"
    })
    void register_invalidRegistration_returnsExpectedErrorWithoutSideEffects(
            boolean studentExists, boolean active, boolean courseExists, String expectedResult) {
        Student student = studentExists
                ? new Student(STUDENT_ID, EMAIL, active, List.of("F.CSA101"))
                : null;
        when(repository.findStudent(STUDENT_ID)).thenReturn(student);
        lenient().when(repository.courseExists(COURSE_ID)).thenReturn(courseExists);

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals(expectedResult, result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    private void stubSuccessfulRegistration() {
        Student student = new Student(STUDENT_ID, EMAIL, true, List.of("F.CSA101"));
        when(repository.findStudent(STUDENT_ID)).thenReturn(student);
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("F.CSA101"));
    }
}
