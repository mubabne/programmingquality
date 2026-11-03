# Лаборатори 08 — Mockito mock объектууд

Оюутан: **Ч. Мөнхбаяр**

Оюутны код: **B242270045**

Хичээл: **F.CSA313 — Программ хангамжийн чанарын баталгаа ба тест**

`CourseRegistrationService.register()` методыг `StudentRepository` болон `EmailSender` хамаарлаас нь тусгаарлан тестлэв. Хоёр хамаарлыг Mockito mock объектоор орлуулж, бодит өгөгдлийн сан болон SMTP сервер ажиллуулахгүйгээр буцах утга, exception, хадгалалт, и-мэйл илгээлтийг шалгасан. Эцсийн үр дүн: **10 тестийн гүйцэтгэл амжилттай, өгсөн 3 мутаци бүгд илэрсэн**.

## Ажлын орчин

Windows 11, OpenJDK 21.0.8, Apache Maven 3.9.11 ашиглав. Төсөл Java 17 түвшинд хөрвөхөөр тохируулагдсан; тестийн сангууд нь JUnit Jupiter 5.10.2, Mockito 5.11.0.

`java -version` командын бодит гаралт:

```text
openjdk version "21.0.8" 2025-07-15 LTS
OpenJDK Runtime Environment Microsoft-11933218 (build 21.0.8+9-LTS)
OpenJDK 64-Bit Server VM Microsoft-11933218 (build 21.0.8+9-LTS, mixed mode, sharing)
```

`mvn -version` командын бодит гаралт:

```text
Apache Maven 3.9.11 (3e54c93a704957b63ee3494413a2b544fd3d825b)
Maven home: C:\Users\Munkhbayar.ch\Desktop\shutis\programmingquality\.tools\apache-maven-3.9.11
Java version: 21.0.8, vendor: Microsoft, runtime: C:\Program Files\Android\openjdk\jdk-21.0.8
Default locale: en_US, platform encoding: UTF-8
OS name: "windows 11", version: "10.0", arch: "amd64", family: "windows"
```

## Ажиллуулах

OpenJDK 17 буюу түүнээс дээш хувилбар шаардлагатай. Maven суусан бол төслийн үндсэн хавтсанд `mvn test` ажиллуулна. Maven суулгаагүй орчинд доорх wrapper Maven 3.9.11-ийг эхний удаа татаж ашиглана.

Linux болон macOS:

```sh
./mvnw test
```

Windows PowerShell:

```powershell
.\mvnw.cmd test
```

Энэ компьютерт Java нь PATH-д нэмэгдээгүй тул ажиллуулахын өмнө JDK-ийн замыг тохируулсан:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\openjdk\jdk-21.0.8'
```

Java 21 дээр Mockito-ийн inline mock maker ажиллахад dynamic Java agent-ийн анхааруулга гарсан. Энэ нь тестийн алдаа биш бөгөөд эцсийн гүйцэтгэл `BUILD SUCCESS` болсон.

## Хэсэг A Тестүүд

Тестийн класс: [CourseRegistrationServiceTest.java](src/test/java/mn/edu/must/sqat/CourseRegistrationServiceTest.java).

`@ExtendWith(MockitoExtension.class)` нь `@Mock` хамаарлуудыг тест бүрд үүсгэж, `@BeforeEach` нь тэдгээрээр жинхэнэ `CourseRegistrationService` объектыг байгуулна. Энгийн тестүүд өөрт хэрэгтэй өгөгдлийг stub хийсэн; амжилттай бүртгэлийн гурван тест нийтлэг бэлтгэлийн метод ашигласан.

| ID | Тестийн метод | Шалгасан үр дүн, хэрэглэсэн хэрэгсэл |
| --- | --- | --- |
| A1 | `register_activeStudent_returnsOkAndSavesRegistrationAndSendsEmail` | Идэвхтэй, өмнөх холбоо хичээлээ судалсан оюутанд `OK`; `when`, `verify` ашиглан хадгалалт болон и-мэйлийн хаяг, гарчиг, биеийг шалгасан. |
| A2 | `register_missingStudent_returnsErrorWithoutSavingOrSendingEmail` | Олдоогүй оюутанд `ERROR_NO_STUDENT`; `never()`-ээр хадгалалт болон и-мэйл илгээлт хийгдээгүйг шалгасан. |
| A3 | `register_inactiveStudent_returnsErrorWithoutEmailInteractions` | Идэвхгүй оюутанд `ERROR_INACTIVE_STUDENT`; хадгалалт байхгүй, `verifyNoInteractions`-аар и-мэйлтэй огт харьцаагүйг шалгасан. |
| A4 | `register_missingPrerequisites_returnsErrorWithoutSaving` | Хоёр шаардлагатай хичээлийн нэгийг судлаагүй үед `ERROR_PREREQUISITES`; хадгалалт болон и-мэйл илгээлт байхгүй. |
| A5 | `register_emailSenderThrows_returnsOkAndKeepsRegistration` | `doThrow`-оор и-мэйл илгээгчийг алдаатай болгосон ч `OK` буцаж, бүртгэл хадгалагдсан. |
| A6 | `register_nullArguments_throwsWithoutDependencyInteractions` | `assertAll`, `assertThrows` ашиглан null аргумент бүрийг шалгасан; хамаарлуудтай огт харьцаагүй. |
| A7 | `register_successfulRegistration_emailBodyContainsCourseCode` | `ArgumentCaptor<String>`-оор и-мэйлийн биеийг барьж, `F.CSA313` код орсныг шалгасан. |
| A8 | `register_invalidRegistration_returnsExpectedErrorWithoutSideEffects` | `@ParameterizedTest`, `@CsvSource`-ийн 3 мөрөөр оюутан байхгүй, оюутан идэвхгүй, хичээл байхгүй тохиолдлыг шалгасан; хадгалалт болон и-мэйл илгээлт байхгүй. |

A6 нь `register(null, COURSE_ID)`, `register(STUDENT_ID, null)`, `register(null, null)` гэсэн гурван дуудалтад тус бүр `IllegalArgumentException` хүлээнэ. Эдгээр нь нэг `@Test` доторх шалгалтууд тул A6 нэг гүйцэтгэлд тоологдоно. A8-ийн мөрүүдийн дараалал нь `ERROR_NO_STUDENT`, `ERROR_INACTIVE_STUDENT`, `ERROR_NO_COURSE`.

Нийт **7 энгийн тест + A8-ийн 3 мөр = 10 гүйцэтгэл**. Matcher ашигласан дуудлагад бүх аргументыг matcher болгосон; жишээлбэл A7-д `eq(EMAIL)`, `eq(SUBJECT)`, `bodyCaptor.capture()` хамт хэрэглэсэн.

### A8 дахь lenient тохиргоо

A8-ийн эхний хоёр мөр оюутны шалгалтаас шууд буцдаг учраас нийтлэг `courseExists` stub ашиглагдахгүй бөгөөд strict stubs горимд `UnnecessaryStubbingException` үүсэх боломжтой. Зөвхөн уг stub-д `lenient().when(...)` хэрэглэж, бусад stub-ийн strict шалгалтыг хэвээр үлдээсэн.

### Амжилттай гүйцэтгэл

Мутациудын дараа эх кодыг сэргээгээд `mvnw.cmd --batch-mode --no-transfer-progress -Dstyle.color=never clean test` ажиллуулсан. Бүрэн гаралт: [results/mvn-test.txt](results/mvn-test.txt).

```text
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Логийг дахин хадгалах PowerShell команд:

```powershell
New-Item -ItemType Directory -Path results -Force | Out-Null
.\mvnw.cmd --batch-mode --no-transfer-progress -Dstyle.color=never clean test 2>&1 |
    Tee-Object -FilePath results/mvn-test.txt
```

## Хэсэг B Мини мутаци

Мутаци бүрийг эх кодын анхны хувилбар дээр тусад нь оруулж `clean test` ажиллуулсан. Ажиллуулалт бүрийн дараа `CourseRegistrationService.java` файлыг сэргээсэн; эцэст нь `src/main` дахь бүх файл starter-ийн эхтэй байт бүрээрээ ижил байгааг шалгав.

| Мутант | Түр өөрчилсөн код | Унасан тестүүд | Tests / Failures / Errors / Skipped | Бүрэн лог |
| --- | --- | --- | --- | --- |
| 1 | `if (!student.active()) ...` шалгалтыг хассан. | A3; A8-ийн 2-р мөр буюу идэвхгүй оюутан. | 10 / 2 / 0 / 0, `BUILD FAILURE` | [mvn-test-mutant-1.txt](results/mvn-test-mutant-1.txt) |
| 2 | `repository.saveRegistration(studentId, courseId)` дуудалтыг хассан. | A1, A5, A7. | 10 / 3 / 0 / 0, `BUILD FAILURE` | [mvn-test-mutant-2.txt](results/mvn-test-mutant-2.txt) |
| 3 | Эхний нөхцөлөөс `courseId == null` шалгалтыг хассан. | A6. | 10 / 1 / 0 / 0, `BUILD FAILURE` | [mvn-test-mutant-3.txt](results/mvn-test-mutant-3.txt) |

Мутант 1-д идэвхгүй оюутны шалгалтыг алгассан тул `ERROR_INACTIVE_STUDENT`-ийн оронд `ERROR_NO_COURSE` буцаж, A3 болон A8-ийн хоёр дахь мөрийн `assertEquals` унасан. Логт A8 нь `register_invalidRegistration_returnsExpectedErrorWithoutSideEffects(boolean, boolean, boolean, String)[2]` гэсэн гүйцэтгэлээр тэмдэглэгдсэн.

Мутант 2-д `OK` буцаж, и-мэйл илгээгдсээр байсан ч A1, A5, A7-ийн `verify(repository).saveRegistration(...)` унасан. Иймээс зөвхөн буцах утгыг шалгасан тест хадгалалт алга болсныг илрүүлэхгүй нь харагдсан.

Мутант 3-д `register(STUDENT_ID, null)` дуудалт exception шидэхээ больсон тул A6-ийн `assertThrows` унасан. A6-д хоёр аргументыг тус тусад нь null болгож шалгах тохиолдлыг анхнаас нь оруулсан учраас мутацийн дараа тестийг нэмэлтээр засах шаардлага гараагүй. Зөвхөн `register(null, null)` шалгасан бол үлдсэн `studentId == null` нөхцөлөөс болж энэ гэмтлийг илрүүлэхгүй байсан.

Гурван мутант бүгд бодит assertion эсвэл `verify` шалгалтаар илэрсэн; `Errors` нь бүх логт 0 тул `UnnecessaryStubbingException`-д тулгуурлан унасан тест байхгүй. Эдгээр үр дүн нь өгсөн гурван өөрчлөлтийг тестүүд барьж байгааг харуулна; боломжит бүх гэмтлийг бүрэн илрүүлнэ гэсэн баталгаа биш.

## Дүгнэлт

Mock нь урьдчилан тохируулсан хариу буцааж, дуудалтыг шалгах боломж олгодог бол fake нь санах ойд өгөгдөл хадгалах зэрэг хялбаршуулсан ажилладаг хэрэгжүүлэлт юм. Энэ лабораторид өгөгдлийн сан болон и-мэйл илгээгчийг mock болгосноор гаднын үйлчилгээний төлөвөөс хамаарахгүй нэгжийн тест ажиллуулсан. `Student` зэрэг энгийн өгөгдлийн объект, жагсаалт болон цэвэр тооцооллыг mock болгох шаардлагагүй. Тестийн бай болох `CourseRegistrationService`-ийг mock болгож орлуулбал жинхэнэ логикийг шалгахгүй тул бодит объектыг ашигласан. `verify` нь бүртгэл хадгалагдах, мэдэгдэл илгээгдэх зэрэг шаардлагатай үйлдлийг батлахад хэрэгтэй. Харин дотоод дуудалт бүрийн тоо, дарааллыг хэт нарийн тулгавал зөв refactor хийсэн ч тест унаж, засвар үйлчилгээний зардал нэмэгдэнэ. Гурван мутаци нь алдааны код, хадгалалтын дуудалт болон аргумент тус бүрийн null шалгалтыг хамтад нь батлахын ач холбогдлыг харуулсан. Иймээс буцах утга, exception болон шаардлагатай гадаад үйлдлийг тэнцвэртэй шалгах нь ганц төрлийн assertion-д найдахаас илүү үр дүнтэй гэж дүгнэв.

## Нэмэлт даалгавар CI

[![Java tests](https://github.com/mubabne/programmingquality/actions/workflows/test.yml/badge.svg?branch=main)](https://github.com/mubabne/programmingquality/actions/workflows/test.yml)

[test.yml](.github/workflows/test.yml) нь push болон pull request бүрд Ubuntu орчинд Eclipse Temurin 17 суулгаж, Maven wrapper-аар `clean test` ажиллуулна. Тестүүдийг хөгжүүлсэн Windows болон JDK 21 орчноос өөр орчинд автоматаар шалгах боломжтой.

## Ашигласан баримт бичиг

- [JUnit 5.10.2 User Guide](https://docs.junit.org/5.10.2/user-guide/)
- [Mockito 5.11.0 API](https://javadoc.io/static/org.mockito/mockito-core/5.11.0/org/mockito/Mockito.html)
