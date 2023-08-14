import './TeacherEditClasses.scss';
import '../../../libs/IkigaiProjectBuilder/IkigaiProjectBuilder.scss';

import {Add, Clear, Edit} from '@mui/icons-material';
import {
  Autocomplete,
  Button,
  Checkbox,
  Chip,
  Grid,
  TextField,
} from '@mui/material';
import {CLASS_SORTER, KNOWLEDGE_AND_SKILL_SORTER} from '../../../libs/sorters';
import {DefaultPage} from '../../../libs/DefaultPage/DefaultPage';
import {GlobalStateContext} from '../../../libs/GlobalState';
import {addClassName, spread} from '../../../libs/tags';
import {
  class_management_service,
  pl_types,
  project_management,
} from '../../../generated/protobuf-js';
import {createService} from '../../../libs/protos';
import {useContext, useEffect, useState} from 'react';
import {useDelayedAction} from '../../../libs/delayed_action';
import {useFormFields} from '../../../libs/forms';
import {KnowledgeAndSkillModal} from '../../../libs/KnowledgeAndSkillModal/KnowledgeAndSkillModal';
import {replaceInPlace, replaceOrAddInPlace} from '../../../libs/misc';
import ClassManagementService = class_management_service.ClassManagementService;
import IClassX = pl_types.IClassX;
import IKnowledgeAndSkill = pl_types.IKnowledgeAndSkill;
import ProjectManagementService = project_management.ProjectManagementService;
import Type = pl_types.KnowledgeAndSkill.Type;

export function TeacherEditClasses() {
  const global = useContext(GlobalStateContext);

  const [classes, setClasses] = useState<IClassX[]>([]);
  const [selectedClass, setSelectedClass] = useState<IClassX | null>(null);
  const [sortedKnowledgeAndSkills, setSortedKnowledgeAndSkills] = useState<
    IKnowledgeAndSkill[]
  >([]);
  const [classSaveStatus, setClassSaveStatus] = useState<string>('');

  // --- AutoSave ---
  const autoSave = useDelayedAction(
    () => setClassSaveStatus('Modified'),
    () => {
      setClassSaveStatus('Saving...');
      if (selectedClass?.id != null && classFormFields.verifyOk(true)) {
        classFormFields.getValuesObject(true, selectedClass);
        setClassSaveStatus('Saved');
        // return createService(ClassManagementService, 'ClassManagementService')
        //   .saveClass(classFormFields.getValuesObject())
        //   .then(() => setClassSaveStatus('Saved'))
        //   .catch(global.setError);
      } else {
        setClassSaveStatus('Invalid values, Not saved');
      }
      return;
    },
    1000
  );

  // --- Class Form ---

  const classFormFields = useFormFields({onChange: () => autoSave.trigger()});
  const className = classFormFields.useStringFormField('name', {
    maxLength: 255,
  });
  const classNumber = classFormFields.useStringFormField('number', {
    maxLength: 16,
  });
  const classEks = classFormFields.useAutocompleteFormField<
    IKnowledgeAndSkill[]
  >('eks', {isAutocomplete: {isMultiple: true}});
  const classPeriod = classFormFields.useStringFormField('period', {
    maxLength: 16,
  });
  const classGrade = classFormFields.useStringFormField('grade', {
    maxLength: 16,
  });
  const classShortDescr = classFormFields.useStringFormField('shortDescr', {
    maxLength: 255,
  });
  const classLongDescr = classFormFields.useStringFormField('longDescr', {
    maxLength: 255,
  });

  // --- New / Edit EKS Form ---

  const [editKs, setEditKs] = useState<IKnowledgeAndSkill | undefined>();
  const [editKsOp, setEditKsOp] = useState<'ADD' | 'UPDATE'>('ADD');

  // --- Effects ---

  useEffect(() => {
    createService(ProjectManagementService, 'ProjectManagementService')
      .getKnowledgeAndSkills({types: [Type.EKS]})
      .then(response =>
        setSortedKnowledgeAndSkills(
          response.knowledgeAndSkills.sort(KNOWLEDGE_AND_SKILL_SORTER)
        )
      )
      .catch(global.setError);
  }, []);

  useEffect(() => {
    setSelectedClass(null);
    if (global.user == null) {
      setClasses([]);
      return;
    }
    createService(ClassManagementService, 'ClassManagementService')
      .getClasses({teacherId: global.user.teacherId})
      .then(response => setClasses(response.classes))
      .catch(global.setError);
  }, [global.user]);

  useEffect(() => {
    classFormFields.setValuesObject(selectedClass ?? {});
    classFormFields.setEnabled(selectedClass?.id != null);
  }, [selectedClass]);

  if (!global.requireUser(user => user?.isTeacher || user?.isAdmin)) {
    return <></>;
  }

  return (
    <>
      <DefaultPage title="Edit Classes">
        <Grid container spacing={2}>
          <Grid item xs={12} className="section-heading">
            <div className="section-title">Select Class:</div>
          </Grid>
          <Grid item {...spread({sm: 12, md: 7})}>
            <Autocomplete
              autoHighlight
              autoFocus
              value={selectedClass}
              options={classes.sort(CLASS_SORTER)}
              isOptionEqualToValue={(option, value) => option.id === value.id}
              //eslint-disable-next-line @typescript-eslint/no-unused-vars
              renderOption={(props, option, {selected}) => (
                <li {...props} key={option.id}>
                  {option.number}:&nbsp;
                  <i>{option.name}</i>
                </li>
              )}
              renderInput={params => (
                <TextField
                  {...params}
                  label="Select class"
                  size="small"
                  variant="outlined"
                />
              )}
              getOptionLabel={option =>
                (option?.number ?? 'undefined') +
                ': ' +
                (option?.name ?? 'undefined')
              }
              onChange={(e, option) => {
                autoSave.forceDelayedAction(() => {
                  setSelectedClass(option);
                });
              }}
            />
          </Grid>
          <Grid
            item
            {...spread({sm: 12, md: 5})}
            className="teacher-edit-classes-expand-buttons"
          >
            <Button variant="contained" startIcon={<Add />}>
              New Class
            </Button>
            <Button
              variant="contained"
              color="warning"
              disabled={selectedClass == null}
              startIcon={<Clear />}
            >
              Delete Class
            </Button>
          </Grid>
        </Grid>
        <form style={{paddingTop: '2em'}} {...classFormFields.formParams()}>
          <Grid container spacing={2}>
            <Grid item xs={12} className="section-heading">
              <div className="section-title">Edit Class:</div>
              <div className="section-links">
                <div
                  style={{display: selectedClass == null ? 'none' : undefined}}
                >
                  {classSaveStatus}
                </div>
              </div>
            </Grid>
            <Grid item {...spread({sm: 12, md: 4})}>
              <TextField
                required
                label="Class ID"
                {...classNumber.textFieldParams()}
              />
            </Grid>
            <Grid item {...spread({sm: 12, md: 8})}>
              <TextField
                required
                label="Class Name"
                {...className.textFieldParams()}
              />
            </Grid>
            <Grid item {...spread({sm: 12, md: 9.5})}>
              <Autocomplete
                multiple
                autoHighlight
                disableCloseOnSelect
                options={sortedKnowledgeAndSkills}
                isOptionEqualToValue={(option, value) => option.id === value.id}
                renderInput={params => (
                  <TextField
                    label="Select knowledge and skills"
                    autoComplete="off"
                    {...classEks.textFieldParams(params)}
                  />
                )}
                renderOption={(props, option, {selected}) => (
                  <li {...props} key={option.id}>
                    <Checkbox style={{marginRight: 8}} checked={selected} />
                    {option.name}:&nbsp;
                    <i>{option.shortDescr ?? 'undefined'}</i>{' '}
                    {(option.userXId === global.user?.userXId ||
                      global.user?.isAdmin) && (
                      <Edit
                        onClick={e => {
                          setEditKsOp('UPDATE');
                          setEditKs(option);
                          e.stopPropagation();
                        }}
                      />
                    )}
                  </li>
                )}
                getOptionLabel={option =>
                  option.name + ': ' + (option?.shortDescr ?? 'undefined')
                }
                renderTags={(tagValue, getTagProps) =>
                  tagValue.map((option, index) => (
                    <Chip
                      label={option.name}
                      size="small"
                      variant="outlined"
                      {...addClassName(
                        getTagProps({index}),
                        'teacher-edit-classes-chips'
                      )}
                    />
                  ))
                }
                {...classEks.autocompleteParams()}
              />
            </Grid>
            <Grid item {...spread({sm: 12, md: 2.5})}>
              <Button
                variant="contained"
                className="teacher-edit-classes-expand-buttons"
                startIcon={<Add />}
                onClick={() => {
                  setEditKsOp('ADD');
                  setEditKs({type: Type.EKS});
                }}
                disabled={classFormFields.getDisabled()}
              >
                New EKS
              </Button>
            </Grid>
            <Grid item {...spread({sm: 12, md: 6})}>
              <TextField label="Period" {...classPeriod.textFieldParams()} />
            </Grid>
            <Grid item {...spread({sm: 12, md: 6})}>
              <TextField label="Grade" {...classGrade.textFieldParams()} />
            </Grid>
            <Grid item xs={12}>
              <TextField
                label="Short Description"
                {...classShortDescr.textFieldParams()}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                label="Long Description"
                {...classLongDescr.textFieldParams()}
              />
            </Grid>
          </Grid>
        </form>
        <KnowledgeAndSkillModal
          open={editKs != null}
          value={editKs}
          title={
            editKsOp === 'ADD'
              ? 'Create a New Knowledge and Skill'
              : 'Edit Knowledge and Skill'
          }
          okText={editKsOp === 'ADD' ? 'Add EKS' : 'Update EKS'}
          onClose={() => setEditKs(undefined)}
          onOk={ks => {
            createService(ProjectManagementService, 'ProjectManagementService')
              .upsertKnowledgeAndSkill({knowledgeAndSkill: ks})
              .then(response => {
                setSortedKnowledgeAndSkills(
                  replaceOrAddInPlace(
                    [...sortedKnowledgeAndSkills],
                    response.knowledgeAndSkill!,
                    e => e.id
                  ).sort(KNOWLEDGE_AND_SKILL_SORTER)
                );
                if (editKsOp === 'ADD') {
                  // Add the knowledge and skill to the class.
                  classEks.setValue(
                    replaceOrAddInPlace(
                      [...classEks.getValue()!],
                      response.knowledgeAndSkill!,
                      e => e.id
                    ).sort(KNOWLEDGE_AND_SKILL_SORTER)
                  );
                } else {
                  classEks.setValue(
                    replaceInPlace(
                      [...classEks.getValue()!],
                      response.knowledgeAndSkill!,
                      e => e.id
                    ).sort(KNOWLEDGE_AND_SKILL_SORTER)
                  );
                }
              })
              .catch(global.setError)
              .finally(() => setEditKs(undefined));
          }}
        />
      </DefaultPage>
    </>
  );
}
