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
import java.time.Instant;

@Entity(name = Assignment.ENTITY_NAME)
@Table(name = Assignment.TABLE_NAME, schema = "leo_temp")
public class Assignment {

  public static final String ENTITY_NAME = "Assignment";
  public static final String TABLE_NAME = "assignment";
  public static final String COLUMN_ID_NAME = "id";
  public static final String COLUMN_CREATIONTIME_NAME = "creation_time";
  public static final String COLUMN_NAME_NAME = "name";
  public static final String COLUMN_SHORTDESCR_NAME = "short_descr";
  public static final String COLUMN_SHORTDESCRQUILL_NAME = "short_descr_quill";
  public static final String COLUMN_LONGDESCR_NAME = "long_descr";
  public static final String COLUMN_LONGDESCRQUILL_NAME = "long_descr_quill";

  private Integer id;

  private Instant creationTime;

  private String name;

  private String shortDescr;

  private String shortDescrQuill;

  private String longDescr;

  private String longDescrQuill;

  private Class classField;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = COLUMN_ID_NAME, nullable = false)
  public Integer getId() {
    return id;
  }

  public Assignment setId(Integer id) {
    this.id = id;
    return this;
  }

  @Column(name = COLUMN_CREATIONTIME_NAME, nullable = false)
  public Instant getCreationTime() {
    return creationTime;
  }

  public Assignment setCreationTime(Instant creationTime) {
    this.creationTime = creationTime;
    return this;
  }

  @Column(name = COLUMN_NAME_NAME, nullable = false)
  public String getName() {
    return name;
  }

  public Assignment setName(String name) {
    this.name = name;
    return this;
  }

  @Lob
  @Column(name = COLUMN_SHORTDESCR_NAME, nullable = false)
  public String getShortDescr() {
    return shortDescr;
  }

  public Assignment setShortDescr(String shortDescr) {
    this.shortDescr = shortDescr;
    return this;
  }

  @Lob
  @Column(name = COLUMN_SHORTDESCRQUILL_NAME, nullable = false)
  public String getShortDescrQuill() {
    return shortDescrQuill;
  }

  public Assignment setShortDescrQuill(String shortDescrQuill) {
    this.shortDescrQuill = shortDescrQuill;
    return this;
  }

  @Lob
  @Column(name = COLUMN_LONGDESCR_NAME, nullable = false)
  public String getLongDescr() {
    return longDescr;
  }

  public Assignment setLongDescr(String longDescr) {
    this.longDescr = longDescr;
    return this;
  }

  @Lob
  @Column(name = COLUMN_LONGDESCRQUILL_NAME, nullable = false)
  public String getLongDescrQuill() {
    return longDescrQuill;
  }

  public Assignment setLongDescrQuill(String longDescrQuill) {
    this.longDescrQuill = longDescrQuill;
    return this;
  }

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "class_id", nullable = false)
  public Class getClassField() {
    return classField;
  }

  public Assignment setClassField(Class classField) {
    this.classField = classField;
    return this;
  }
}
