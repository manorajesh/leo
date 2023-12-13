package org.davincischools.leo.server.utils.task_queue.workers.project_generators;

import static org.davincischools.leo.database.utils.DaoUtils.ifInitialized;
import static org.davincischools.leo.database.utils.DaoUtils.listIfInitialized;
import static org.davincischools.leo.database.utils.DaoUtils.sortByPosition;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.davincischools.leo.database.daos.Project;
import org.davincischools.leo.database.daos.ProjectInput;
import org.davincischools.leo.database.utils.Database;
import org.davincischools.leo.database.utils.repos.GetAssignmentsParams;
import org.davincischools.leo.database.utils.repos.GetProjectInputsParams;

@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
public class ProjectGeneratorInput {

  private ProjectInput projectInput;
  /* Ordered project input values grouped by ordered categories. */
  private List<ProjectDefinitionCategoryInputValues> sortedProjectInputs;
  private Project fillInProject;

  public static @Nullable ProjectGeneratorInput getProjectGeneratorInput(
      Database db, int projectInputId) {
    ProjectInput projectInput =
        Iterables.getOnlyElement(
            db.getProjectInputRepository()
                .getProjectInputs(
                    new GetProjectInputsParams()
                        .setProjectInputIds(List.of(projectInputId))
                        .setIncludeProcessing(true)
                        .setIncludeAssignment(
                            new GetAssignmentsParams().setIncludeKnowledgeAndSkills(true))),
            null);
    if (projectInput == null) {
      return null;
    }

    var generatorInput = new ProjectGeneratorInput(projectInput, new ArrayList<>(), null);
    sortByPosition(
            listIfInitialized(projectInput.getProjectDefinition().getProjectDefinitionCategories()))
        .forEach(
            category -> {
              generatorInput
                  .getSortedProjectInputs()
                  .add(new ProjectDefinitionCategoryInputValues(category, new ArrayList<>()));
            });

    var indexedDefinitionCategory =
        Maps.uniqueIndex(
            generatorInput.getSortedProjectInputs(), p -> p.getDefinitionCategory().getId());
    sortByPosition(listIfInitialized(projectInput.getProjectInputValues()))
        .forEach(
            inputValue -> {
              ifInitialized(inputValue.getProjectDefinitionCategory())
                  .ifPresent(
                      definitionCategory -> {
                        Objects.requireNonNull(
                                indexedDefinitionCategory.get(definitionCategory.getId()))
                            .getInputValues()
                            .add(inputValue);
                      });
            });

    return generatorInput;
  }
}