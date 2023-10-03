import {useNavigate} from 'react-router';
import {useContext, useEffect} from 'react';
import {GlobalStateContext} from '../../libs/GlobalState';

export function RedirectToDashboard() {
  const global = useContext(GlobalStateContext);

  const navigate = useNavigate();
  useEffect(() => {
    if (global.userX?.isAdminX) {
      navigate('/dashboards/admin-dashboard.html');
    } else if (global.userX?.isTeacher) {
      navigate('/dashboards/teacher-dashboard.html');
    } else if (global.userX?.isStudent) {
      navigate('/dashboards/student-dashboard.html');
    } else {
      navigate('/projects/all-projects.html');
    }
  });

  return <></>;
}
