import {Modal} from '@mui/material';
import {user_x_management} from '../../generated/protobuf-js';
import IFullUserXDetails = user_x_management.IFullUserXDetails;
import {SearchForUserX} from './SearchForUserX';
import {DeepReadonly} from '../misc';
import {TitledPaper} from '../TitledPaper/TitledPaper';
import {FilterAltTwoTone} from '@mui/icons-material';

export function SearchForUserXModal(props: {
  showSearchBox: boolean;
  title: string;
  onSelect: (userX?: DeepReadonly<IFullUserXDetails>) => void;
  baseRequest: DeepReadonly<user_x_management.IGetUserXsRequest>;
}) {
  return (
    <Modal
      open={props.showSearchBox}
      onClose={() => {
        props.onSelect(undefined);
      }}
      style={{
        top: '50%',
        left: '50%',
        transform: 'translate(-50%, -50%)',
        minWidth: '40rem',
        height: '30rem',
      }}
    >
      <TitledPaper
        title={props.title}
        icon={<FilterAltTwoTone />}
        highlightColor="orange"
        bodyStyle={{
          padding: '1rem',
        }}
        paperProps={{
          elevation: 10,
        }}
      >
        <SearchForUserX
          onSelect={props.onSelect}
          baseRequest={props.baseRequest}
        />
      </TitledPaper>
    </Modal>
  );
}
