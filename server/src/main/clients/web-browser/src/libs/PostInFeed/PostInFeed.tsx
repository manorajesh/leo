import './PostInFeed.scss';

import {pl_types} from '../../generated/protobuf-js';
import IProjectPost = pl_types.IProjectPost;
import {toLong} from '../misc';
import ReactQuill from 'react-quill';
import {AccountCircle} from '@mui/icons-material';

export function PostInFeed(props: {post: IProjectPost}) {
  return (
    <>
      <div className="global-flex-column post-in-feed-post">
        <div
          className="global-flex-row"
          style={{justifyContent: 'space-between'}}
        >
          <div
            className="global-flex-row"
            style={{alignItems: 'center', gap: '0.5em'}}
          >
            <div>
              <AccountCircle />
            </div>
            <div>
              <span className="post-in-feed-name">
                {props.post?.userX?.firstName ?? ''}&nbsp;
                {props.post?.userX?.lastName ?? ''}
              </span>
              <br />
              <span className="post-in-feed-title">
                {(props.post?.name?.trim()?.length ?? 0) > 0
                  ? props.post?.name ?? ''
                  : 'Untitled'}
                &nbsp;
              </span>
            </div>
          </div>
          <div className="post-in-feed-date">
            {new Date(
              toLong(props.post?.postTimeMs ?? 0).toNumber()
            ).toLocaleDateString()}
          </div>
        </div>
        <div className="post-in-feed-content">
          <ReactQuill
            theme="snow"
            className="global-react-quill"
            value={
              (props.post?.longDescrHtml?.trim()?.length ?? 0) > 0 &&
              props.post?.longDescrHtml !== '<p><br></p>'
                ? props.post?.longDescrHtml ?? ''
                : 'No Content'
            }
            preserveWhitespace={true}
            modules={{toolbar: false}}
            style={{padding: 0}}
            readOnly={true}
          />
        </div>
      </div>
    </>
  );
}