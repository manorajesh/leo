package org.davincischools.leo.server.utils;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.davincischools.leo.database.daos.Assignment;
import org.davincischools.leo.database.daos.ClassX;
import org.davincischools.leo.database.daos.KnowledgeAndSkill;
import org.davincischools.leo.database.daos.Motivation;
import org.davincischools.leo.database.daos.Project;
import org.davincischools.leo.database.daos.ProjectDefinitionCategoryType;
import org.davincischools.leo.database.daos.ProjectMilestone;
import org.davincischools.leo.database.daos.ProjectMilestoneStep;
import org.davincischools.leo.database.daos.ProjectPost;
import org.davincischools.leo.database.daos.School;
import org.davincischools.leo.database.daos.UserX;
import org.davincischools.leo.database.utils.Database;
import org.davincischools.leo.database.utils.repos.ClassXRepository.FullClassX;
import org.davincischools.leo.database.utils.repos.ProjectDefinitionRepository.FullProjectDefinition;
import org.davincischools.leo.database.utils.repos.ProjectInputRepository.FullProjectInput;
import org.davincischools.leo.database.utils.repos.ProjectRepository.MilestoneWithSteps;
import org.davincischools.leo.database.utils.repos.ProjectRepository.ProjectWithMilestones;
import org.davincischools.leo.database.utils.repos.UserXRepository;
import org.davincischools.leo.protos.pl_types.KnowledgeAndSkill.Type;
import org.davincischools.leo.protos.pl_types.Project.ThumbsState;
import org.davincischools.leo.protos.pl_types.ProjectDefinition.State;
import org.davincischools.leo.protos.pl_types.ProjectInputValue;
import org.davincischools.leo.protos.user_management.FullUserDetails;

public class DataAccess {

  @SafeVarargs
  public static <T> T coalesce(Callable<T>... values) throws NullPointerException {
    return Stream.of(values)
        .map(
            value -> {
              try {
                return value.call();
              } catch (Exception e) {
                return null;
              }
            })
        .filter(Objects::nonNull)
        .findFirst()
        .orElseThrow(NullPointerException::new);
  }

  public static org.davincischools.leo.protos.pl_types.User convertFullUserXToProto(UserX user) {
    var userProto = org.davincischools.leo.protos.pl_types.User.newBuilder();
    if (user.getId() != null) {
      userProto
          .setUserXId(user.getId())
          .setFirstName(user.getFirstName())
          .setLastName(user.getLastName())
          .setEmailAddress(user.getEmailAddress());
    }
    if (UserXRepository.isAdmin(user)) {
      userProto.setIsAdmin(true).setAdminId(user.getAdminX().getId());
    }
    if (UserXRepository.isTeacher(user)) {
      userProto.setIsTeacher(true).setTeacherId(user.getTeacher().getId());
    }
    if (UserXRepository.isStudent(user)) {
      userProto.setIsStudent(true).setStudentId(user.getStudent().getId());
    }
    if (UserXRepository.isDemo(user)) {
      userProto.setIsDemo(true);
    }
    if (UserXRepository.isAuthenticated(user)) {
      userProto.setIsAuthenticated(true);
    }
    if (user.getDistrict() != null && user.getDistrict().getId() != null) {
      userProto.setDistrictId(user.getDistrict().getId());
    }
    return userProto.build();
  }

  public static FullUserDetails convertFullUserXToDetailsProto(UserX userX) {
    var proto = FullUserDetails.newBuilder().setUser(convertFullUserXToProto(userX));
    if (UserXRepository.isStudent(userX)) {
      if (userX.getStudent().getDistrictStudentId() != null) {
        proto.setDistrictStudentId(userX.getStudent().getDistrictStudentId());
      }
      if (userX.getStudent().getGrade() != null) {
        proto.setStudentGrade(userX.getStudent().getGrade());
      }
    }
    return proto.build();
  }

  public static List<org.davincischools.leo.protos.pl_types.User> getProtoFullUserXsByDistrictId(
      Database db, int districtId) {
    return StreamSupport.stream(
            db.getUserXRepository().findAllByDistrictId(districtId).spliterator(), false)
        .map(DataAccess::convertFullUserXToProto)
        .collect(Collectors.toList());
  }

  public static org.davincischools.leo.protos.pl_types.School convertSchoolToProto(School school) {
    return org.davincischools.leo.protos.pl_types.School.newBuilder()
        .setId(coalesce(school::getId, () -> -1))
        .setDistrictId(coalesce(school.getDistrict()::getId, () -> -1))
        .setName(school.getName())
        .setAddress(Strings.nullToEmpty(school.getAddress()))
        .build();
  }

  public static List<org.davincischools.leo.protos.pl_types.School> getProtoSchoolsByDistrictId(
      Database db, int districtId) {
    return StreamSupport.stream(
            db.getSchoolRepository().findAllByDistrictId(districtId).spliterator(), false)
        .map(DataAccess::convertSchoolToProto)
        .collect(Collectors.toList());
  }

  public static org.davincischools.leo.protos.pl_types.ClassX.Builder toFullClassXProto(
      FullClassX fullClassX) {
    var classX = fullClassX.classX();
    return org.davincischools.leo.protos.pl_types.ClassX.newBuilder()
        .setId(classX.getId())
        .setName(classX.getName())
        .setNumber(classX.getNumber())
        .setGrade(coalesce(classX::getGrade, () -> ""))
        .setPeriod(coalesce(classX::getPeriod, () -> ""))
        .setShortDescr(coalesce(classX::getShortDescr, () -> ""))
        .setLongDescrHtml(coalesce(classX::getLongDescrHtml, () -> ""))
        .addAllKnowledgeAndSkills(
            fullClassX.knowledgeAndSkills().stream()
                .map(DataAccess::toKnowledgeAndSkillProto)
                .map(org.davincischools.leo.protos.pl_types.KnowledgeAndSkill.Builder::build)
                .toList());
  }

  public static org.davincischools.leo.protos.pl_types.KnowledgeAndSkill.Builder
      toKnowledgeAndSkillProto(KnowledgeAndSkill knowledgeAndSkill) {
    return org.davincischools.leo.protos.pl_types.KnowledgeAndSkill.newBuilder()
        .setId(knowledgeAndSkill.getId())
        .setType(Type.valueOf(knowledgeAndSkill.getType()))
        .setName(knowledgeAndSkill.getName())
        .setCategory(coalesce(knowledgeAndSkill::getCategory, () -> ""))
        .setShortDescr(coalesce(knowledgeAndSkill::getShortDescr, () -> ""))
        .setLongDescrHtml(coalesce(knowledgeAndSkill::getLongDescrHtml, () -> ""))
        .setGlobal(Boolean.TRUE.equals(knowledgeAndSkill.getGlobal()))
        .setUserXId(knowledgeAndSkill.getUserX().getId());
  }

  public static org.davincischools.leo.protos.pl_types.Assignment convertAssignmentToProto(
      ClassX classX, Assignment assignment) {
    return org.davincischools.leo.protos.pl_types.Assignment.newBuilder()
        .setId(coalesce(assignment::getId, () -> -1))
        .setName(assignment.getName())
        .setShortDescr(coalesce(assignment::getShortDescr, () -> ""))
        .setLongDescrHtml(coalesce(assignment::getLongDescrHtml, () -> ""))
        .setClassX(DataAccess.toFullClassXProto(new FullClassX(classX, new ArrayList<>())))
        .build();
  }

  public static org.davincischools.leo.protos.pl_types.ProjectDefinition.Builder
      convertFullProjectDefinition(FullProjectDefinition fullProjectDefinition) {
    var projectDefinitionProto =
        org.davincischools.leo.protos.pl_types.ProjectDefinition.newBuilder()
            .setId(fullProjectDefinition.definition().getId())
            .setName(fullProjectDefinition.definition().getName())
            .setTemplate(Boolean.TRUE.equals(fullProjectDefinition.definition().getTemplate()));
    for (var categoryDao : fullProjectDefinition.categories()) {
      var type = categoryDao.getProjectDefinitionCategoryType();
      createProjectInputValueProto(
          categoryDao.getId(), type, projectDefinitionProto.addInputsBuilder());
    }
    return projectDefinitionProto;
  }

  public static org.davincischools.leo.protos.pl_types.ProjectDefinition.Builder
      convertFullProjectInput(FullProjectInput fullProjectInput) {
    var projectDefinitionProto =
        org.davincischools.leo.protos.pl_types.ProjectDefinition.newBuilder()
            .setId(fullProjectInput.definition().getId())
            .setName(fullProjectInput.definition().getName())
            .setInputId(fullProjectInput.input().getId())
            .setState(State.valueOf(fullProjectInput.input().getState()))
            .setTemplate(Boolean.TRUE.equals(fullProjectInput.definition().getTemplate()));
    for (var categoryValues : fullProjectInput.values()) {
      var type = categoryValues.type();
      var inputValueProto =
          createProjectInputValueProto(
              categoryValues.category().getId(), type, projectDefinitionProto.addInputsBuilder());
      switch (inputValueProto.getCategory().getValueType()) {
        case FREE_TEXT -> inputValueProto.addAllFreeTexts(
            categoryValues.values().stream()
                .map(org.davincischools.leo.database.daos.ProjectInputValue::getFreeTextValue)
                .toList());
        case EKS, XQ_COMPETENCY -> inputValueProto.addAllSelectedIds(
            categoryValues.values().stream()
                .map(
                    org.davincischools.leo.database.daos.ProjectInputValue
                        ::getKnowledgeAndSkillValue)
                .map(KnowledgeAndSkill::getId)
                .toList());
        case MOTIVATION -> inputValueProto.addAllSelectedIds(
            categoryValues.values().stream()
                .map(org.davincischools.leo.database.daos.ProjectInputValue::getMotivationValue)
                .map(Motivation::getId)
                .toList());
        case UNSET -> throw new IllegalStateException("Unset value type");
      }
    }
    return projectDefinitionProto;
  }

  private static ProjectInputValue.Builder createProjectInputValueProto(
      int categoryId,
      ProjectDefinitionCategoryType type,
      ProjectInputValue.Builder projectInputValueProto) {
    projectInputValueProto
        .getCategoryBuilder()
        .setId(categoryId)
        .setShortDescr(type.getShortDescr())
        .setInputDescr(type.getInputDescr())
        .setName(type.getName())
        .setHint(type.getHint())
        .setPlaceholder(type.getInputPlaceholder())
        .setValueType(
            org.davincischools.leo.protos.pl_types.ProjectInputCategory.ValueType.valueOf(
                type.getValueType()));
    return projectInputValueProto;
  }

  public static org.davincischools.leo.protos.pl_types.Project convertProjectToProto(
      Project project) {
    return org.davincischools.leo.protos.pl_types.Project.newBuilder()
        .setId(coalesce(project::getId, () -> -1))
        .setName(project.getName())
        .setShortDescr(coalesce(project::getShortDescr, () -> ""))
        .setLongDescrHtml(coalesce(project::getLongDescrHtml, () -> ""))
        .setFavorite(Boolean.TRUE.equals(project.getFavorite()))
        .setThumbsState(
            ThumbsState.valueOf(coalesce(project::getThumbsState, ThumbsState.UNSET::name)))
        .setThumbsStateReason(coalesce(project::getThumbsStateReason, () -> ""))
        .setArchived(Boolean.TRUE.equals(project.getArchived()))
        .setActive(Boolean.TRUE.equals(project.getActive()))
        .build();
  }

  public static org.davincischools.leo.protos.pl_types.Project.Milestone
      convertProjectMilestoneToProto(ProjectMilestone projectMilestone) {
    return org.davincischools.leo.protos.pl_types.Project.Milestone.newBuilder()
        .setId(projectMilestone.getId())
        .setName(projectMilestone.getName())
        .build();
  }

  public static org.davincischools.leo.protos.pl_types.Project.Milestone.Step
      convertProjectMilestoneStepToProto(ProjectMilestoneStep projectMilestoneStep) {
    return org.davincischools.leo.protos.pl_types.Project.Milestone.Step.newBuilder()
        .setId(projectMilestoneStep.getId())
        .setName(projectMilestoneStep.getName())
        .build();
  }

  public static org.davincischools.leo.protos.pl_types.Project.Milestone
      convertMilestoneWithStepsToProto(MilestoneWithSteps milestone) {
    return convertProjectMilestoneToProto(milestone.milestone()).toBuilder()
        .addAllSteps(
            milestone.steps().stream().map(DataAccess::convertProjectMilestoneStepToProto).toList())
        .build();
  }

  public static org.davincischools.leo.protos.pl_types.Project convertProjectWithMilestonesToProto(
      ProjectWithMilestones projectWithMilestones) {
    return convertProjectToProto(projectWithMilestones.project()).toBuilder()
        .addAllMilestones(
            projectWithMilestones.milestones().stream()
                .map(DataAccess::convertMilestoneWithStepsToProto)
                .toList())
        .build();
  }

  public static org.davincischools.leo.protos.pl_types.ProjectPost convertProjectPostToProto(
      ProjectPost projectPost) {
    return org.davincischools.leo.protos.pl_types.ProjectPost.newBuilder()
        .setUser(convertFullUserXToProto(projectPost.getUserX()))
        .setName(projectPost.getName())
        .setMessageHtml(projectPost.getMessageHtml())
        .setPostEpochSec((int) projectPost.getCreationTime().getEpochSecond())
        .build();
  }
}
