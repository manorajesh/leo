package org.davincischools.leo.database.utils.repos;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.davincischools.leo.database.daos.AssignmentProjectDefinition;
import org.davincischools.leo.database.daos.ProjectDefinition;
import org.davincischools.leo.database.daos.ProjectDefinitionCategory;
import org.davincischools.leo.database.daos.ProjectDefinitionCategory_;
import org.davincischools.leo.database.daos.ProjectDefinition_;
import org.davincischools.leo.database.daos.UserX;
import org.davincischools.leo.database.utils.QueryHelper.QueryHelperUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProjectDefinitionRepository
    extends JpaRepository<ProjectDefinition, Integer>, AutowiredRepositoryValues {

  record FullProjectDefinition(
      ProjectDefinition definition,
      Optional<Instant> selected,
      List<ProjectDefinitionCategory> categories) {}

  record FullProjectDefinitionRow(
      @Nullable AssignmentProjectDefinition assignment,
      ProjectDefinition definition,
      ProjectDefinitionCategory category) {}

  default ProjectDefinition upsert(String name, UserX userX, Consumer<ProjectDefinition> modifier) {
    checkArgument(!Strings.isNullOrEmpty(name));
    checkNotNull(modifier);

    ProjectDefinition projectDefinition =
        findByName(name)
            .orElseGet(() -> new ProjectDefinition().setCreationTime(Instant.now()))
            .setUserX(userX)
            .setName(name);

    modifier.accept(projectDefinition);

    return saveAndFlush(projectDefinition);
  }

  // TODO: For development, remove.
  Optional<ProjectDefinition> findByName(String name);

  @Query(
      """
      SELECT NULL, d, c
      FROM ProjectDefinition d
      LEFT JOIN FETCH d.userX
      LEFT JOIN FETCH ProjectDefinitionCategory c
      ON d.id = c.projectDefinition.id
      LEFT JOIN FETCH c.projectDefinitionCategoryType
      WHERE d.id = (:projectDefinitionId)
      ORDER BY d.id, c.position, c.id""")
  List<FullProjectDefinitionRow> findFullProjectDefinitionRows(
      @Param("projectDefinitionId") int projectDefinitionId);

  default Optional<FullProjectDefinition> findFullProjectDefinitionById(int projectDefinitionId) {
    return Optional.ofNullable(
        Iterables.getOnlyElement(
            toFullProjectDefinitions(findFullProjectDefinitionRows(projectDefinitionId)), null));
  }

  @Query(
      """
      SELECT apd, d, c
      FROM AssignmentProjectDefinition apd
      LEFT JOIN FETCH apd.projectDefinition d
      LEFT JOIN FETCH d.userX
      LEFT JOIN ProjectDefinitionCategory c
      ON d.id = c.projectDefinition.id
      LEFT JOIN FETCH c.projectDefinitionCategoryType
      WHERE apd.assignment.id = (:assignmentId)
      ORDER BY apd.selected DESC, d.id, c.position, c.id""")
  List<FullProjectDefinitionRow> findFullProjectDefinitionRowsByAssignmentId(
      @Param("assignmentId") int assignmentId);

  default List<FullProjectDefinition> findFullProjectDefinitionsByAssignmentId(int assignmentId) {
    return toFullProjectDefinitions(findFullProjectDefinitionRowsByAssignmentId(assignmentId));
  }

  private List<FullProjectDefinition> toFullProjectDefinitions(
      Iterable<FullProjectDefinitionRow> rows) {
    List<FullProjectDefinition> allDefinitions = new ArrayList<>();
    FullProjectDefinition definition = null;

    for (FullProjectDefinitionRow row : rows) {
      if (definition == null
          || !Objects.equals(definition.definition.getId(), row.definition().getId())) {
        allDefinitions.add(
            definition =
                new FullProjectDefinition(
                    checkNotNull(row.definition()),
                    Optional.ofNullable(row.assignment())
                        .map(AssignmentProjectDefinition::getSelected),
                    new ArrayList<>()));
      }
      if (row.category() != null) {
        definition.categories.add(checkNotNull(row.category()));
      }
    }

    return allDefinitions;
  }

  @Modifying
  @Transactional
  @Query(
      "UPDATE ProjectDefinition p SET p.userX.id = (:userXId) WHERE p.id = (:id) AND p.userX.id IS"
          + " NULL")
  void updateUserX(@Param("id") int id, @Param("userXId") int userXId);

  default List<ProjectDefinition> getProjectDefinitions(GetProjectDefinitionsParams params) {
    checkNotNull(params);

    return getQueryHelper()
        .query(
            ProjectDefinition.class,
            (u, projectDefinition, builder) ->
                configureQuery(u, projectDefinition, builder, params));
  }

  public static void configureQuery(
      QueryHelperUtils u,
      From<?, ProjectDefinition> projectDefinition,
      CriteriaBuilder builder,
      GetProjectDefinitionsParams params) {
    checkNotNull(u);
    checkNotNull(projectDefinition);
    checkNotNull(builder);
    checkNotNull(params);

    u.notDeleted(projectDefinition);

    u.notDeleted(u.fetch(projectDefinition, ProjectDefinition_.userX, JoinType.LEFT));

    var projectDefinitionCategory =
        u.fetch(
            projectDefinition,
            ProjectDefinition_.projectDefinitionCategories,
            JoinType.LEFT,
            ProjectDefinitionCategory::getProjectDefinition,
            ProjectDefinition::setProjectDefinitionCategories);

    u.fetch(
        projectDefinitionCategory,
        ProjectDefinitionCategory_.projectDefinitionCategoryType,
        JoinType.LEFT);
  }
}
