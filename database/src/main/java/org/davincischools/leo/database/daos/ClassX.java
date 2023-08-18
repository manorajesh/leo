package org.davincischools.leo.database.daos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;

@Entity(name = ClassX.ENTITY_NAME)
@Table(name = ClassX.TABLE_NAME, schema = "leo_temp")
public class ClassX implements Serializable {

  public static final String ENTITY_NAME = "ClassX";
  public static final String TABLE_NAME = "class_x";
  public static final String COLUMN_ID_NAME = "id";
  public static final String COLUMN_CREATIONTIME_NAME = "creation_time";
  public static final String COLUMN_DELETED_NAME = "deleted";
  public static final String COLUMN_NAME_NAME = "name";
  public static final String COLUMN_NUMBER_NAME = "number";
  public static final String COLUMN_PERIOD_NAME = "period";
  public static final String COLUMN_GRADE_NAME = "grade";
  public static final String COLUMN_SHORTDESCR_NAME = "short_descr";
  public static final String COLUMN_LONGDESCRHTML_NAME = "long_descr_html";
  private static final long serialVersionUID = -1516051238272452857L;

  private Integer id;

  private Instant creationTime;

  private Instant deleted;

  private String name;

  private String number;

  private String period;

  private String grade;

  private String shortDescr;

  private String longDescrHtml;

  private School school;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = COLUMN_ID_NAME, nullable = false)
  public Integer getId() {
    return id;
  }

  public ClassX setId(Integer id) {
    this.id = id;
    return this;
  }

  @Column(name = COLUMN_CREATIONTIME_NAME, nullable = false)
  public Instant getCreationTime() {
    return creationTime;
  }

  public ClassX setCreationTime(Instant creationTime) {
    this.creationTime = creationTime;
    return this;
  }

  @Column(name = COLUMN_DELETED_NAME)
  public Instant getDeleted() {
    return deleted;
  }

  public ClassX setDeleted(Instant deleted) {
    this.deleted = deleted;
    return this;
  }

  @Column(name = COLUMN_NAME_NAME, nullable = false)
  public String getName() {
    return name;
  }

  public ClassX setName(String name) {
    this.name = name;
    return this;
  }

  @Column(name = COLUMN_NUMBER_NAME, nullable = false, length = 16)
  public String getNumber() {
    return number;
  }

  public ClassX setNumber(String number) {
    this.number = number;
    return this;
  }

  @Column(name = COLUMN_PERIOD_NAME, length = 16)
  public String getPeriod() {
    return period;
  }

  public ClassX setPeriod(String period) {
    this.period = period;
    return this;
  }

  @Column(name = COLUMN_GRADE_NAME, length = 16)
  public String getGrade() {
    return grade;
  }

  public ClassX setGrade(String grade) {
    this.grade = grade;
    return this;
  }

  @Lob
  @Column(name = COLUMN_SHORTDESCR_NAME)
  public String getShortDescr() {
    return shortDescr;
  }

  public ClassX setShortDescr(String shortDescr) {
    this.shortDescr = shortDescr;
    return this;
  }

  @Lob
  @Column(name = COLUMN_LONGDESCRHTML_NAME)
  public String getLongDescrHtml() {
    return longDescrHtml;
  }

  public ClassX setLongDescrHtml(String longDescrHtml) {
    this.longDescrHtml = longDescrHtml;
    return this;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "school_id")
  public School getSchool() {
    return school;
  }

  public ClassX setSchool(School school) {
    this.school = school;
    return this;
  }
}
