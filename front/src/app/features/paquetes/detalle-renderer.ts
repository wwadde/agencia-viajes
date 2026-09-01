import { generateHTML, type JSONContent } from '@tiptap/core';
import StarterKit from '@tiptap/starter-kit';

export type PaqueteDetalleContent = string | JSONContent | null | undefined;

export function renderDetalleHtml(detalle: PaqueteDetalleContent): string | null {
  if (detalle == null) {
    return null;
  }

  if (typeof detalle === 'string') {
    const trimmed = detalle.trim();
    if (!trimmed) {
      return null;
    }

    const parsed = tryParseJsonContent(trimmed);
    if (parsed) {
      return renderJsonContentHtml(parsed);
    }

    return `<p>${escapeHtml(trimmed)}</p>`;
  }

  if (isJsonContent(detalle)) {
    return renderJsonContentHtml(detalle);
  }

  return null;
}

function renderJsonContentHtml(value: JSONContent): string {
  const canUseDom = typeof window !== 'undefined' && typeof document !== 'undefined';

  if (canUseDom) {
    try {
      return generateHTML(value, [StarterKit]);
    } catch {
      // Fall through to a safe server-compatible serializer when the browser DOM is unavailable.
    }
  }

  return renderJsonNode(value);
}

function renderJsonNode(node: JSONContent): string {
  const children = node.content ?? [];

  switch (node.type) {
    case 'doc':
      return children.map(renderJsonNode).join('');
    case 'paragraph':
      return `<p>${renderInlineContent(children)}</p>`;
    case 'heading': {
      const level = Number((node.attrs as Record<string, unknown> | undefined)?.['level'] ?? 1);
      return `<h${level}>${renderInlineContent(children)}</h${level}>`;
    }
    case 'blockquote':
      return `<blockquote>${children.map(renderJsonNode).join('')}</blockquote>`;
    case 'bulletList':
      return `<ul>${children.map(renderJsonNode).join('')}</ul>`;
    case 'orderedList':
      return `<ol>${children.map(renderJsonNode).join('')}</ol>`;
    case 'listItem':
      return `<li>${children.map(renderJsonNode).join('')}</li>`;
    case 'codeBlock':
      return `<pre><code>${escapeHtml(node.content?.[0]?.text ?? '')}</code></pre>`;
    case 'horizontalRule':
      return '<hr>';
    case 'image': {
      const attrs = (node.attrs as Record<string, unknown> | undefined) ?? {};
      return `<img src="${escapeAttribute(String(attrs['src'] ?? ''))}" alt="${escapeAttribute(String(attrs['alt'] ?? ''))}" />`;
    }
    case 'text':
      return renderInlineText(node);
    case 'hardBreak':
      return '<br>';
    case 'table':
      return `<table>${children.map(renderJsonNode).join('')}</table>`;
    case 'tableRow':
      return `<tr>${children.map(renderJsonNode).join('')}</tr>`;
    case 'tableCell':
      return `<td>${children.map(renderJsonNode).join('')}</td>`;
    case 'tableHeader':
      return `<th>${children.map(renderJsonNode).join('')}</th>`;
    default:
      return children.map(renderJsonNode).join('');
  }
}

function renderInlineContent(nodes: JSONContent[] = []): string {
  return nodes.map(renderJsonNode).join('');
}

function renderInlineText(node: JSONContent): string {
  let text = escapeHtml(node.text ?? '');

  for (const mark of node.marks ?? []) {
    switch (mark.type) {
      case 'bold':
        text = `<strong>${text}</strong>`;
        break;
      case 'italic':
        text = `<em>${text}</em>`;
        break;
      case 'underline':
        text = `<u>${text}</u>`;
        break;
      case 'strike':
        text = `<s>${text}</s>`;
        break;
      case 'code':
        text = `<code>${text}</code>`;
        break;
      case 'link': {
        const href = escapeAttribute(String((mark.attrs as Record<string, unknown> | undefined)?.['href'] ?? '#'));
        text = `<a href="${href}" target="_blank" rel="noopener noreferrer">${text}</a>`;
        break;
      }
      default:
        break;
    }
  }

  return text;
}

function tryParseJsonContent(value: string): JSONContent | null {
  try {
    const parsed: unknown = JSON.parse(value);
    return isJsonContent(parsed) ? parsed : null;
  } catch {
    return null;
  }
}

function isJsonContent(value: unknown): value is JSONContent {
  if (typeof value !== 'object' || value === null) {
    return false;
  }

  const withType = value as { type?: unknown };
  return typeof withType.type === 'string';
}

function escapeHtml(value: string): string {
  return value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

function escapeAttribute(value: string): string {
  return escapeHtml(value).replaceAll('`', '&#96;');
}
