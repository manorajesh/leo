import {useContext} from 'react';
import {GlobalStateContext} from '../../libs/GlobalStateProvider/GlobalStateProvider';
import {ProjectBuilder as MainProjectBuilder} from '../../libs/ProjectBuilder/ProjectBuilder';

export function ProjectBuilderTab() {
  const global = useContext(GlobalStateContext);
  const userX = global.useUserXLogin(
    'You must be logged in to view this dashboard.'
  );

  if (!userX) {
    return <></>;
  }

  return (
    <>
      <MainProjectBuilder noCategoriesText="There are no categories for the project builder." />
    </>
  );
}
