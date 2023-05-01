package org.davincischools.leo.database.test;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.davincischools.leo.database.daos.Assignment;
import org.davincischools.leo.database.daos.ClassX;
import org.davincischools.leo.database.daos.District;
import org.davincischools.leo.database.daos.KnowledgeAndSkill;
import org.davincischools.leo.database.daos.School;
import org.davincischools.leo.database.daos.UserX;
import org.davincischools.leo.database.utils.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestData {

  public static final String PASSWORD = "password";
  // This is random in order to avoid collisions with other test instances.
  public static final AtomicInteger counter =
      new AtomicInteger(new Random().nextInt(Integer.MAX_VALUE - 1000));

  private final Database db;

  private UserX teacher;
  private UserX student;
  private UserX admin;

  private ClassX chemistryClassX;
  private KnowledgeAndSkill chemistryEks1, chemistryEks2;
  private Assignment chemistryAssignment1, chemistryAssignment2;

  private ClassX programmingClassX;
  private KnowledgeAndSkill programmingEks1, programmingEks2;
  private Assignment programmingAssignment1, programmingAssignment2;

  public TestData(@Autowired Database db) {
    this.db = db;
  }

  public UserX getTeacher() {
    return teacher;
  }

  public UserX getStudent() {
    return student;
  }

  public UserX getAdmin() {
    return admin;
  }

  public ClassX getChemistryClassX() {
    return chemistryClassX;
  }

  public KnowledgeAndSkill getChemistryEks1() {
    return chemistryEks1;
  }

  public KnowledgeAndSkill getChemistryEks2() {
    return chemistryEks2;
  }

  public Assignment getChemistryAssignment1() {
    return chemistryAssignment1;
  }

  public Assignment getChemistryAssignment2() {
    return chemistryAssignment2;
  }

  public ClassX getProgrammingClassX() {
    return programmingClassX;
  }

  public KnowledgeAndSkill getProgrammingEks1() {
    return programmingEks1;
  }

  public KnowledgeAndSkill getProgrammingEks2() {
    return programmingEks2;
  }

  public Assignment getProgrammingAssignment1() {
    return programmingAssignment1;
  }

  public Assignment getProgrammingAssignment2() {
    return programmingAssignment2;
  }

  public void addTestData(int count) {
    // Rather than delete what's there, which could be dangerous since it is
    // possible that a misconfiguration could point this to a real database,
    // we create new users each time with unique ids.

    District district = db.createDistrict("Wiseburn Unified School District " + count);

    db.createSchool(district, "DVC");
    db.createSchool(district, "DVConnect");
    db.createSchool(district, "DVD");
    db.createSchool(district, "DVFlex");
    db.createSchool(district, "DVRise");
    School school = db.createSchool(district, "DVS");

    admin =
        db.createUserX(
            district,
            "sahendrickson-" + count + "@gmail.com",
            userX -> userX.setFirstName("Scott").setLastName("Hendrickson"));
    db.addAdminXPermission(admin);

    teacher =
        db.createUserX(
            district,
            "seno-" + count + "@davincischools.org",
            userX -> userX.setFirstName("Steven").setLastName("Eno"));

    student =
        db.createUserX(
            district,
            "swallis=" + count + "@davincischools.org",
            userX -> userX.setFirstName("Steve").setLastName("Wallis"));

    db.addAdminXPermission(admin);
    db.addTeacherPermission(teacher);
    db.addStudentPermission(
        userX -> userX.getStudent().setStudentId(counter.incrementAndGet()).setGrade(count),
        admin,
        teacher,
        student);

    db.addTeachersToSchool(school, teacher.getTeacher());
    db.addStudentsToSchool(school, admin.getStudent(), teacher.getStudent(), student.getStudent());

    programmingClassX = db.createClassX(school, "Intro to Programming " + count, classX -> {});
    db.addTeachersToClassX(programmingClassX, teacher.getTeacher());
    db.addStudentsToClassX(
        programmingClassX, admin.getStudent(), teacher.getStudent(), student.getStudent());

    programmingEks1 =
        db.createKnowledgeAndSkill(
            programmingClassX,
            "Sort Functions",
            "I understand and can implement different sort functions.");
    programmingAssignment1 =
        db.createAssignment(programmingClassX, "Implement sort algorithms.", programmingEks1);

    programmingEks2 =
        db.createKnowledgeAndSkill(
            programmingClassX, "Containers", "I can use Lists, Sets, and Maps.");
    programmingAssignment2 =
        db.createAssignment(programmingClassX, "Implement sort algorithms.", programmingEks1);

    chemistryClassX = db.createClassX(school, "Intro to Chemistry " + count, classX -> {});
    db.addTeachersToClassX(chemistryClassX, teacher.getTeacher());
    db.addStudentsToClassX(
        chemistryClassX, admin.getStudent(), teacher.getStudent(), student.getStudent());

    chemistryEks1 =
        db.createKnowledgeAndSkill(
            chemistryClassX,
            "Periodic Table",
            "I can recognize the basic elements on a periodic table.");
    chemistryAssignment1 = db.createAssignment(chemistryClassX, "Periodic Table", chemistryEks1);

    chemistryEks2 =
        db.createKnowledgeAndSkill(
            chemistryClassX,
            "Valence Electrons",
            "I can determine the number of valence electrons for each element.");
    chemistryAssignment2 = db.createAssignment(chemistryClassX, "Valence Electrons", chemistryEks2);
  }
}
