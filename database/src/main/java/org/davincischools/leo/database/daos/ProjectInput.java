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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Accessors(chain = true)
@Entity(name = ProjectInput.ENTITY_NAME)
@Table(name = ProjectInput.TABLE_NAME, schema = "leo_temp")
public class ProjectInput implements Serializable {

  public static final String ENTITY_NAME = "ProjectInput";
  public static final String TABLE_NAME = "project_input";
  public static final String COLUMN_ID_NAME = "id";
  public static final String COLUMN_CREATIONTIME_NAME = "creation_time";
  public static final String COLUMN_DELETED_NAME = "deleted";
  public static final String COLUMN_TIMEOUT_NAME = "timeout";
  public static final String COLUMN_STATE_NAME = "state";
  private static final long serialVersionUID = 5495351023374530153L;

  private Integer id;

  private Instant creationTime;

  private Instant deleted;

  private Instant timeout;

  private String state;

  private ProjectDefinition projectDefinition;

  private Assignment assignment;

  private UserX userX;

  private Set<LogReference> logReferences = new LinkedHashSet<>();

  private Set<Project> projects = new LinkedHashSet<>();

  private Set<ProjectInputValue> projectInputValues = new LinkedHashSet<>();

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = COLUMN_ID_NAME, nullable = false)
  public Integer getId() {
    return id;
  }

  @Column(name = COLUMN_CREATIONTIME_NAME, nullable = false)
  public Instant getCreationTime() {
    return creationTime;
  }

  @Column(name = COLUMN_DELETED_NAME)
  public Instant getDeleted() {
    return deleted;
  }

  @Column(name = COLUMN_TIMEOUT_NAME)
  public Instant getTimeout() {
    return timeout;
  }

  @Lob
  @Column(name = COLUMN_STATE_NAME, nullable = false)
  public String getState() {
    return state;
  }

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "project_definition_id", nullable = false)
  public ProjectDefinition getProjectDefinition() {
    return projectDefinition;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assignment_id")
  public Assignment getAssignment() {
    return assignment;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_x_id")
  public UserX getUserX() {
    return userX;
  }

  @OneToMany(mappedBy = "projectInput")
  public Set<LogReference> getLogReferences() {
    return logReferences;
  }

  @OneToMany(mappedBy = "projectInput")
  public Set<Project> getProjects() {
    return projects;
  }

  @OneToMany(mappedBy = "projectInput")
  public Set<ProjectInputValue> getProjectInputValues() {
    return projectInputValues;
  }
}
