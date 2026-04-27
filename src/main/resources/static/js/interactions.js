(function () {
  const csrfToken  = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

  /* ── Fecha "hasta" por defecto = hoy ── */
  const inputTo = document.getElementById('inputTo');
  if (inputTo && !inputTo.value) {
    inputTo.value = new Date().toISOString().split('T')[0];
  }

  /* ── NDA filter highlight ── */
  const ndaCb    = document.getElementById('ndaFilterCb');
  const ndaLabel = document.getElementById('ndaFilterLabel');
  function syncNdaLabel() {
    ndaLabel.style.borderColor = ndaCb.checked ? '#168a53' : 'var(--line)';
    ndaLabel.style.background  = ndaCb.checked ? '#e8f5e9' : '#fff';
    ndaLabel.style.color       = ndaCb.checked ? '#168a53' : 'inherit';
  }
  syncNdaLabel();
  ndaCb.addEventListener('change', syncNdaLabel);

  /* ── Pastillas de estado ── */
  document.querySelectorAll('.filter-pill').forEach(pill => {
    const cb = pill.previousElementSibling;
    function sync() { cb.checked ? pill.classList.add('active') : pill.classList.remove('active'); }
    sync();
    cb.addEventListener('change', sync);
  });

  /* ── Toast ── */
  const toast = document.getElementById('copyToast');
  let toastTimer;
  function showToast(msg) {
    toast.textContent = msg + ' ✓';
    toast.classList.add('show');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => toast.classList.remove('show'), 1800);
  }

  /* ── Copiar al click ── */
  document.addEventListener('click', async function (e) {
    if (e.target.closest('.comment-display')) return;
    if (e.target.closest('.wa-link')) return;
    const el = e.target.closest('.copyable');
    if (!el) return;
    if (el.closest('tr[data-no-molestar]')) return;
    const text = el.getAttribute('data-copy') || el.textContent.trim();
    try { await navigator.clipboard.writeText(text); }
    catch (_) {
      const ta = document.createElement('textarea');
      ta.value = text; ta.style.cssText = 'position:fixed;left:-9999px';
      document.body.appendChild(ta); ta.select();
      document.execCommand('copy'); document.body.removeChild(ta);
    }
    showToast('Copiado: ' + text);
  });

  /* ── Email copy ── */
  const copyBtn      = document.getElementById('copyEmailsBtn');
  const clearBtn     = document.getElementById('clearEmailsBtn');
  const counter      = document.getElementById('emailsCountInline');
  const selectAllCb  = document.getElementById('selectAllEmails');
  const selectAllTxt = document.getElementById('selectAllText');

  function normalize(e) { return (e || '').trim().toLowerCase(); }
  function getSelectedEmails() {
    const set = new Set();
    document.querySelectorAll('.email-item:checked').forEach(cb => {
      const e = normalize(cb.getAttribute('data-email'));
      if (e) set.add(e);
    });
    return Array.from(set).sort();
  }
  function getAllEmailItems()         { return Array.from(document.querySelectorAll('.email-item')); }
  function getSelectableEmailItems() {
    return getAllEmailItems().filter(cb =>
      cb.closest('tr[data-excluido]') === null &&
      cb.closest('tr[data-no-molestar]') === null
    );
  }
  function syncSelectAll() {
    const sel    = getSelectableEmailItems();
    const marked = sel.filter(cb => cb.checked);
    if (!sel.length || !marked.length) {
      selectAllCb.checked = false; selectAllCb.indeterminate = false;
      selectAllTxt.textContent = 'Seleccionar todos';
    } else if (marked.length === sel.length) {
      selectAllCb.checked = true; selectAllCb.indeterminate = false;
      selectAllTxt.textContent = 'Deseleccionar todos';
    } else {
      selectAllCb.checked = false; selectAllCb.indeterminate = true;
      selectAllTxt.textContent = `Seleccionar todos (${marked.length}/${sel.length})`;
    }
  }
  function renderState() {
    const list = getSelectedEmails();
    counter.textContent    = String(list.length);
    copyBtn.disabled       = list.length === 0;
    clearBtn.style.display = list.length === 0 ? 'none' : 'inline-flex';
    syncSelectAll();
    return list;
  }
  function clearSelection() { getAllEmailItems().forEach(cb => cb.checked = false); renderState(); }

  selectAllCb.addEventListener('change', () => {
    getSelectableEmailItems().forEach(cb => cb.checked = selectAllCb.checked);
    renderState();
  });
  document.addEventListener('change', ev => {
    if (ev.target?.classList.contains('email-item')) renderState();
  });
  copyBtn.addEventListener('click', async () => {
    const list = renderState();
    if (!list.length) return;
    const text = list.join('; ');
    try { await navigator.clipboard.writeText(text); }
    catch (_) {
      const ta = document.createElement('textarea');
      ta.value = text; ta.setAttribute('readonly','');
      ta.style.cssText = 'position:fixed;left:-9999px';
      document.body.appendChild(ta); ta.select();
      document.execCommand('copy'); document.body.removeChild(ta);
    }
    clearSelection();
    showToast('Emails copiados');
  });
  clearBtn.addEventListener('click', clearSelection);
  renderState();

  /* ── Dots de estado ── */
  const statusMap = {
    'GRIS_SIN_CONTACTO'     : 'gris',
    'AZUL_VISITA_PROGRAMADA': 'azul-claro',
    'AZUL_VISITA_REALIZADA' : 'azul-oscuro',
    'NARANJA_QUIERE_VISITA' : 'naranja',
    'ROSA_DESCARTA'         : 'rosa',
    'VERDE_PENSANDO'        : 'verde',
    'AMARILLO_OFERTA'       : 'amarillo'
  };
  document.querySelectorAll('.status-dot').forEach(dot => {
    const cls = statusMap[dot.getAttribute('data-status')];
    if (cls) dot.classList.add(cls);
  });

  /* ══ MODAL COMENTARIOS ══ */
  let _commentTargetSpan = null;
  let _commentClientId   = null;
  let _commentIid        = null;

  const commentEditor   = document.getElementById('commentEditor');
  const commentBackdrop = document.getElementById('commentModalBackdrop');

  function sanitizeHtml(html) {
    const div = document.createElement('div');
    div.innerHTML = html;
    const walker = document.createTreeWalker(div, NodeFilter.SHOW_ELEMENT);
    const toUnwrap = [];
    let node = walker.nextNode();
    while (node) {
      const tag = node.tagName.toLowerCase();
      if (tag !== 'mark' && tag !== 'br') toUnwrap.push(node);
      node = walker.nextNode();
    }
    toUnwrap.forEach(el => el.replaceWith(...el.childNodes));
    div.querySelectorAll('mark').forEach(m => {
      while (m.attributes.length > 0) m.removeAttribute(m.attributes[0].name);
    });
    return div.innerHTML;
  }

  window.openCommentModal = function(span) {
    _commentTargetSpan = span;
    _commentClientId   = span.getAttribute('data-cid');
    _commentIid        = span.getAttribute('data-iid');
    const rawHtml       = span.innerHTML.trim();
    const isPlaceholder = span.style.color === 'rgb(204, 204, 204)' || rawHtml.includes('(editar)');
    commentEditor.innerHTML = isPlaceholder ? '' : rawHtml;
    commentBackdrop.classList.add('open');
    setTimeout(() => {
      commentEditor.focus();
      const range = document.createRange();
      range.selectNodeContents(commentEditor);
      range.collapse(false);
      const sel = window.getSelection();
      sel.removeAllRanges();
      sel.addRange(range);
    }, 80);
  };

  function closeCommentModal() {
    commentBackdrop.classList.remove('open');
    _commentTargetSpan = null;
  }

  document.getElementById('btnCancelComment').addEventListener('click', closeCommentModal);
  commentBackdrop.addEventListener('click', e => { if (e.target === commentBackdrop) closeCommentModal(); });

  commentEditor.addEventListener('keydown', function(e) {
    if ((e.ctrlKey || e.altKey) && e.key === 'Enter') {
      e.preventDefault(); document.execCommand('insertLineBreak');
    }
    if (e.ctrlKey && e.key.toLowerCase() === 'h') { e.preventDefault(); applyHighlight(); }
  });

  function applyHighlight() {
    commentEditor.focus();
    const sel = window.getSelection();
    if (!sel || sel.isCollapsed) return;
    const range = sel.getRangeAt(0);
    const ancestor = range.commonAncestorContainer;
    const markParent = ancestor.nodeType === 3
      ? ancestor.parentElement?.closest('mark')
      : ancestor.closest?.('mark');
    if (markParent) { markParent.replaceWith(...markParent.childNodes); return; }
    const mark = document.createElement('mark');
    try { range.surroundContents(mark); }
    catch(_) { const f = range.extractContents(); mark.appendChild(f); range.insertNode(mark); }
    sel.removeAllRanges();
  }

  document.getElementById('btnHighlight').addEventListener('click', applyHighlight);

  document.getElementById('btnClearMark').addEventListener('click', function() {
    commentEditor.focus();
    const sel = window.getSelection();
    if (!sel || sel.isCollapsed) return;
    const range = sel.getRangeAt(0);
    const fragment = range.extractContents();
    fragment.querySelectorAll('mark').forEach(m => m.replaceWith(...m.childNodes));
    range.insertNode(fragment);
    sel.removeAllRanges();
  });

  document.getElementById('btnClearAll').addEventListener('click', () => {
    commentEditor.innerHTML = ''; commentEditor.focus();
  });

  document.getElementById('btnSaveComment').addEventListener('click', async function() {
    if (!_commentIid || !_commentClientId) return;
    const html = sanitizeHtml(commentEditor.innerHTML);
    try {
      const res = await fetch(`/clientes/${_commentClientId}/interacciones/${_commentIid}/comments`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
          [csrfHeader]: csrfToken
        },
        body: 'comments=' + encodeURIComponent(html)
      });
      if (!res.ok) throw new Error('HTTP ' + res.status);
      if (_commentTargetSpan) {
        if (!html || html.trim() === '' || html.trim() === '<br>') {
          _commentTargetSpan.innerHTML = '— <span style="font-size:10px; color:#bbb;">(editar)</span>';
          _commentTargetSpan.style.color = '#ccc';
        } else {
          _commentTargetSpan.innerHTML = html;
          _commentTargetSpan.style.color = '#555';
        }
      }
      closeCommentModal();
    } catch(err) {
      alert('Error al guardar el comentario.');
      console.error(err);
    }
  });

  /* ══ MODAL VISITA ══ */
  (function buildTimeSelect() {
    const sel = document.getElementById('visitModalTime');
    for (let h = 8; h < 22; h++) {
      for (let m = 0; m < 60; m += 15) {
        const hh  = String(h).padStart(2, '0');
        const mm  = String(m).padStart(2, '0');
        const opt = document.createElement('option');
        opt.value       = hh + ':' + mm;
        opt.textContent = hh + ':' + mm;
        sel.appendChild(opt);
      }
    }
  })();

  window.openVisitModal = function(btn) {
    document.getElementById('visitModalInteractionId').value  = btn.getAttribute('data-interaction-id');
    document.getElementById('visitModalClientName').value     = btn.getAttribute('data-client-name');
    document.getElementById('visitModalPropertyCode').value   = btn.getAttribute('data-property-code');
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('visitModalDate').value = today;
    const now      = new Date();
    const nextHour = String(now.getHours() + 1).padStart(2, '0') + ':00';
    const sel      = document.getElementById('visitModalTime');
    sel.value      = nextHour;
    if (!sel.value) sel.selectedIndex = 0;
    document.getElementById('visitModalBackdrop').classList.add('open');
  };

  window.closeVisitModal = function() {
    document.getElementById('visitModalBackdrop').classList.remove('open');
  };

  document.getElementById('visitModalBackdrop').addEventListener('click', function(e) {
    if (e.target === this) closeVisitModal();
  });

})();