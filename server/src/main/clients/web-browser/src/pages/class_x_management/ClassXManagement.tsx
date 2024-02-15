import './ClassXManagement.scss';

import {useContext} from 'react';
import {GlobalStateContext} from '../../libs/GlobalStateProvider/GlobalStateProvider';
import {DefaultPage} from '../../libs/DefaultPage/DefaultPage';
import {TabbedPanel} from '../../libs/TabbedPanel/TabbedPanel';
import {OverviewTab} from './OverviewTab';
import {EditClassXsTab} from './EditClassXsTab';
import {EditAssignmentsTab} from './EditAssignmentsTab';

enum ClassManagementTab {
  OVERVIEW,
  EDIT_CLASSES,
  EDIT_ASSIGNMENTS,
}

export function ClassXManagement() {
  const global = useContext(GlobalStateContext);
  const userX = global.useUserXLogin(
    'You must be a teacher to administer classes.',
    userX => userX.isAdminX || userX.isTeacher
  );

  if (!userX) {
    return <></>;
  }

  return (
    <>
      <DefaultPage title="Class Management">
        <div style={{height: '100%'}}>
          <TabbedPanel
            tabKeyEnum={ClassManagementTab}
            defaultTabKey={ClassManagementTab.OVERVIEW}
            tabs={[
              {
                key: ClassManagementTab.OVERVIEW,
                label: 'Overview',
                content: <OverviewTab />,
              },
              {
                key: ClassManagementTab.EDIT_CLASSES,
                label: 'Edit Classes',
                content: <EditClassXsTab />,
              },
              {
                key: ClassManagementTab.EDIT_ASSIGNMENTS,
                label: 'Edit Assignments',
                content: <EditAssignmentsTab />,
              },
            ]}
          />
        </div>
      </DefaultPage>
    </>
  );
}
