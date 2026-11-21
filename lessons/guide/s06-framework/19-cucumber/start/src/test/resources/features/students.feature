# The Students API in plain language. The steps run on the framework's own clients (StudentsApi, StudentData, Cleanup).
Feature: Students

  Scenario: A new student is stored as sent
    Given a new student in "Physics"
    When the student is created
    Then reading the student returns the same data
    And the student is listed in "Physics"

  Scenario Outline: Students can change their email
    Given a new student in "<programme>"
    And the student is created
    When the student's email is changed
    Then reading the student shows the new email

    Examples:
      | programme        |
      | Mathematics      |
      | Computer Science |

  Scenario: A second student with the same email is a conflict
    Given a new student in "Business"
    And the student is created
    When another student with the same email is created
    Then the answer is 409 Conflict
