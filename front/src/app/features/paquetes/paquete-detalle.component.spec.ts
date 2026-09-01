import { describe, expect, it } from 'vitest';
import type { JSONContent } from '@tiptap/core';
import { renderDetalleHtml } from './detalle-renderer';

describe('renderDetalleHtml', () => {
  it('renders Tiptap JSON content without a browser DOM in SSR', () => {
    const detalle: JSONContent = {
      type: 'doc',
      content: [
        {
          type: 'paragraph',
          content: [{ type: 'text', text: 'Hola mundo' }],
        },
        {
          type: 'bulletList',
          content: [
            {
              type: 'listItem',
              content: [{ type: 'paragraph', content: [{ type: 'text', text: 'Primero' }] }],
            },
          ],
        },
      ],
    };

    const html = renderDetalleHtml(detalle);

    expect(html).not.toBeNull();
    expect(html!).toContain('<p>Hola mundo</p>');
    expect(html!).toContain('<ul>');
    expect(html!).toContain('<li>');
    expect(html!).toContain('Primero');
  });
});
