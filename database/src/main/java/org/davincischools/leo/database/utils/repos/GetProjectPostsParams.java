package org.davincischools.leo.database.utils.repos;

import java.util.Optional;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class GetProjectPostsParams {

  @Nullable private Boolean includeTags;
  @Nullable private Boolean includeComments;
  @Nullable private Boolean includeProjects;
  @Nullable private Boolean includeRatings;
  @Nullable private Boolean includeAssignments;

  @Nullable private Iterable<Integer> projectPostIds;
  @Nullable private Iterable<Integer> projectIds;
  @Nullable private Iterable<Integer> assignmentIds;
  @Nullable private Iterable<Integer> classXIds;
  @Nullable private Iterable<Integer> schoolIds;
  @Nullable private Iterable<Integer> userXIds;
  @Nullable private Boolean beingEdited;

  Optional<Boolean> getIncludeTags() {
    return Optional.ofNullable(includeTags);
  }

  Optional<Boolean> getIncludeComments() {
    return Optional.ofNullable(includeComments);
  }

  Optional<Boolean> getIncludeProjects() {
    return Optional.ofNullable(includeProjects);
  }

  Optional<Boolean> getIncludeRatings() {
    return Optional.ofNullable(includeRatings);
  }

  Optional<Boolean> getIncludeAssignments() {
    return Optional.ofNullable(includeAssignments);
  }

  Optional<Iterable<Integer>> getProjectIds() {
    return Optional.ofNullable(projectIds);
  }

  Optional<Iterable<Integer>> getProjectPostIds() {
    return Optional.ofNullable(projectPostIds);
  }

  Optional<Iterable<Integer>> getAssignmentIds() {
    return Optional.ofNullable(assignmentIds);
  }

  Optional<Iterable<Integer>> getClassXIds() {
    return Optional.ofNullable(classXIds);
  }

  Optional<Iterable<Integer>> getSchoolIds() {
    return Optional.ofNullable(schoolIds);
  }

  Optional<Iterable<Integer>> getUserXIds() {
    return Optional.ofNullable(userXIds);
  }

  Optional<Boolean> getBeingEdited() {
    return Optional.ofNullable(beingEdited);
  }
}