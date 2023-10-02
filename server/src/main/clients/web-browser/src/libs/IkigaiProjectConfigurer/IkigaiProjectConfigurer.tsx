import '../global.scss';

import {CSSProperties, useEffect, useState} from 'react';
import {Layout} from 'react-grid-layout';
import {PersistedReactGridLayout} from '../PersistedReactGridLayout/PersistedReactGridLayout';
import {TitledPaper} from '../TitledPaper/TitledPaper';
import {VISIBLE_ALPHA} from '../../Ikigai/Ikigai';
import {pl_types} from '../../generated/protobuf-js';
import Markdown from 'react-markdown';

interface Category {
  category: pl_types.IProjectInputValue;
  selected: boolean;
  headerColor?: string;
  highlightColor: string;
}

export function IkigaiProjectConfigurer(props: {
  allCategories: readonly pl_types.IProjectInputValue[];
  setSelectedCategories: (
    categories: readonly pl_types.IProjectInputValue[]
  ) => void;
  style?: Partial<CSSProperties>;
}) {
  const [categories, setCategories] = useState<readonly Category[]>([]);

  useEffect(() => {
    let i = 0;
    setCategories(
      props.allCategories.map(c => ({
        category: c,
        selected: i++ < 4,
        highlightColor: 'grey',
      }))
    );
    props.setSelectedCategories(props.allCategories);
  }, [props.allCategories]);

  useEffect(() => {
    const selectedCategories: Category[] = [];
    categories.forEach(c => {
      c.headerColor = undefined;
      c.highlightColor = 'grey';
      if (c.selected) {
        selectedCategories.push(c);
      }
    });
    selectedCategories.forEach((c, index) => {
      const hue = index * (360 / selectedCategories.length);
      c.headerColor = `hsla(${hue}, 100%, 75%, ${VISIBLE_ALPHA}`;
      c.highlightColor = `hsla(${hue}, 100%, 75%, 100%)`;
    });
    props.setSelectedCategories(selectedCategories.map(c => c.category));
  }, [categories]);

  function handleDrag(layout: Layout[]) {
    const mappedCategories = new Map<string, Category>(
      categories.map(c => [(c.category?.category?.typeId ?? 0).toString(), c])
    );

    let needsUpdate = false;
    const newCategories = layout
      .sort((a, b) => a.y - b.y)
      .map((l, index) => {
        const c = mappedCategories.get(l.i)!;
        /* eslint-disable-next-line eqeqeq */
        needsUpdate = needsUpdate || categories.at(index) != c;
        return c;
      });
    if (needsUpdate) {
      setCategories(newCategories);
    }
  }

  return (
    <PersistedReactGridLayout
      id="category-grid"
      cols={1}
      rows={3}
      padding={{x: 5, y: 0}}
      gap={{x: 0, y: 16}}
      panels={categories.map((c, index) => ({
        id: (c.category?.category?.typeId ?? 0).toString(),
        panel: (
          <TitledPaper
            title={
              <Markdown className="global-markdown">
                {c.category?.category?.name ?? 'undefined'}
              </Markdown>
            }
            headerColor={c.headerColor}
            highlightColor={c.highlightColor}
            draggableCursorType="ns-resize"
            bodyStyle={{
              display: 'flex',
              flexFlow: 'column nowrap',
              justifyContent: 'space-between',
            }}
            icon={
              <>
                <span
                  style={{
                    whiteSpace: 'nowrap',
                    color: 'black',
                    fontWeight: 'normal',
                  }}
                >
                  Include:&nbsp;
                  <input
                    type="checkbox"
                    checked={c.selected}
                    onChange={() => {
                      c.selected = !c.selected;
                      setCategories([...categories]);
                    }}
                  />
                </span>
              </>
            }
          >
            <Markdown className="global-markdown">
              {c.category?.category?.shortDescr ?? 'undefined'}
            </Markdown>
          </TitledPaper>
        ),
        layout: {x: 0, y: index, w: 1, h: 1},
      }))}
      style={Object.assign(
        {},
        {width: '100%', height: '100%'},
        props.style ?? {}
      )}
      onDrag={handleDrag}
    />
  );
}
