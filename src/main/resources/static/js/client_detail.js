(function () {

  const csrfToken  = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

  /* ══ TABS ══ */
  const tabButtons = document.querySelectorAll('.client-tab[data-target]');
  const tabPanels  = document.querySelectorAll('.client-tab-panel');

  tabButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      const targetSelector = btn.getAttribute('data-target');
      if (!targetSelector) return;
      tabButtons.forEach(b => b.classList.remove('active'));
      tabPanels.forEach(p => p.classList.remove('active'));
      btn.classList.add('active');
      const panel = document.querySelector(targetSelector);
      if (panel) panel.classList.add('active');
      const hash = targetSelector.replace('#tab-', '#');
      if (history.replaceState) history.replaceState(null, '', hash);
      else location.hash = hash;
    });
  });

  function activateFromHash() {
    const hash  = window.location.hash || '#interacciones';
    const id    = hash.replace('#', 'tab-');
    const panel = document.getElementById(id);
    if (!panel) return;
    tabButtons.forEach(b => b.classList.remove('active'));
    tabPanels.forEach(p => p.classList.remove('active'));
    const btn = document.querySelector(`.client-tab[data-target="#${id}"]`);
    if (btn) btn.classList.add('active');
    panel.classList.add('active');
  }

  window.addEventListener('hashchange', activateFromHash);
  activateFromHash();

  /* ══ COMBOBOX ══ */
  const comboInput    = document.getElementById('niComboInput');
  const dropdown      = document.getElementById('niComboDropdown');
  const chip          = document.getElementById('niComboChip');
  const chipLabel     = document.getElementById('niComboChipLabel');
  const comboClearBtn = document.getElementById('niComboClearBtn');
  const wrapper       = document.getElementById('niComboWrapper');

  let currentHighlight = -1;
  let visibleOptions   = [];

  const allItems = (CATALOG || [])
    .filter(p => !p.sold)
    .map(p => ({
      code        : p.propertyCode  || '',
      type        : p.propertyType  || '',
      address     : p.address       || '',
      municipality: p.municipality  || '',
      preVendido  : p.preVendido    || false,
      label       : [p.propertyCode, p.propertyType, p.municipality].filter(Boolean).join(' · ')
    }));

  function escHtml(str) {
    return (str||'').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
  }

  function renderDropdown(q) {
    dropdown.innerHTML = '';
    currentHighlight   = -1;
    const lq = (q || '').trim().toLowerCase();
    visibleOptions = lq === ''
      ? allItems
      : allItems.filter(item =>
          item.code.toLowerCase().includes(lq)         ||
          item.type.toLowerCase().includes(lq)         ||
          item.municipality.toLowerCase().includes(lq) ||
          item.address.toLowerCase().includes(lq)
        );
    if (visibleOptions.length === 0) {
      dropdown.innerHTML = '<div class="combo-empty">Sin resultados</div>';
      dropdown.classList.add('open');
      return;
    }
    visibleOptions.forEach((item, idx) => {
      const div = document.createElement('div');
      div.className   = 'combo-option';
      div.dataset.idx = idx;
      const preTag = item.preVendido
        ? `<span style="margin-left:6px;font-size:10px;color:#a05800;background:#fff4e6;
                        border:1px solid #e07a18;border-radius:999px;padding:1px 6px;font-weight:700;">
             ⏳ Pre vendido
           </span>` : '';
      div.innerHTML = `
        <div class="opt-code">${escHtml(item.code)}${preTag}</div>
        <div class="opt-sub">${escHtml([item.type, item.municipality, item.address].filter(Boolean).join(' · '))}</div>
      `;
      div.addEventListener('mousedown', e => { e.preventDefault(); selectItem(item); });
      dropdown.appendChild(div);
    });
    dropdown.classList.add('open');
  }

  function selectItem(item) {
    document.getElementById('niPropertyCode').value = item.code;
    document.getElementById('niPropertyType').value = item.type;
    document.getElementById('niAddress').value      = item.address;
    document.getElementById('niMunicipality').value = item.municipality;
    comboInput.value      = item.label;
    comboInput.readOnly   = true;
    chipLabel.textContent = item.label;
    chip.style.display    = 'inline-flex';
    closeDropdown();
  }

  function clearSelection() {
    comboInput.value    = '';
    comboInput.readOnly = false;
    chip.style.display  = 'none';
    ['niPropertyCode','niPropertyType','niAddress','niMunicipality'].forEach(id => {
      const el = document.getElementById(id);
      if (el) el.value = '';
    });
  }

  function openDropdown(q) { renderDropdown(q); }
  function closeDropdown()  { dropdown.classList.remove('open'); currentHighlight = -1; }

  function setHighlight(idx) {
    const opts = dropdown.querySelectorAll('.combo-option');
    opts.forEach(o => o.classList.remove('highlighted'));
    if (idx >= 0 && idx < opts.length) {
      opts[idx].classList.add('highlighted');
      opts[idx].scrollIntoView({ block:'nearest' });
      currentHighlight = idx;
    }
  }

  if (comboInput) {
    comboInput.addEventListener('click',   () => { comboInput.readOnly = false; comboInput.value = ''; openDropdown(''); });
    comboInput.addEventListener('input',   () => openDropdown(comboInput.value));
    comboInput.addEventListener('keydown', e => {
      const opts = dropdown.querySelectorAll('.combo-option');
      if      (e.key === 'ArrowDown') { e.preventDefault(); setHighlight(Math.min(currentHighlight+1, opts.length-1)); }
      else if (e.key === 'ArrowUp')   { e.preventDefault(); setHighlight(Math.max(currentHighlight-1, 0)); }
      else if (e.key === 'Enter')     { e.preventDefault(); if (currentHighlight >= 0 && visibleOptions[currentHighlight]) selectItem(visibleOptions[currentHighlight]); }
      else if (e.key === 'Escape')    { closeDropdown(); }
    });
    comboInput.addEventListener('blur', () => setTimeout(closeDropdown, 150));
  }

  if (comboClearBtn) comboClearBtn.addEventListener('click', clearSelection);
  document.addEventListener('click', e => { if (wrapper && !wrapper.contains(e.target)) closeDropdown(); });

  /* ══ PANELES PRE VENDA / COMPRADOR ══ */
  const chkPreVenda   = document.getElementById('chkPreVenda');
  const panelPreVenda = document.getElementById('panelPreVenda');
  if (chkPreVenda && panelPreVenda) {
    chkPreVenda.addEventListener('change', () => {
      panelPreVenda.style.display = chkPreVenda.checked ? 'block' : 'none';
    });
  }
  const chkComprador   = document.getElementById('chkCompradorFinal');
  const panelComprador = document.getElementById('panelInmuebleComprado');
  if (chkComprador && panelComprador) {
    chkComprador.addEventListener('change', () => {
      panelComprador.style.display = chkComprador.checked ? 'block' : 'none';
    });
  }

  /* ══ NDA TOGGLE ══ */
  document.querySelectorAll('.nda-toggle').forEach(cb => {
    cb.addEventListener('change', async function () {
      const clientId      = cb.getAttribute('data-client-id');
      const interactionId = cb.getAttribute('data-interaction-id');
      const checked       = cb.checked;
      try {
        const res = await fetch(`/clientes/${clientId}/interacciones/${interactionId}/nda`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded', [csrfHeader]: csrfToken },
          body: 'ndaRequested=' + encodeURIComponent(String(checked))
        });
        if (!res.ok) throw new Error('HTTP ' + res.status);
      } catch (e) { cb.checked = !checked; alert('No se pudo actualizar NDA.'); }
    });
  });
  
  /* ══ INVALID TOGGLE (phones & emails) ══ */
  document.querySelectorAll('.btn-invalid-toggle').forEach(btn => {
    btn.addEventListener('click', async function () {
      const type      = btn.getAttribute('data-type');   // 'phone' | 'email'
      const id        = btn.getAttribute('data-id');
      const clientId  = btn.getAttribute('data-client-id');
      const isInvalid = btn.getAttribute('data-invalid') === 'true';
      const newVal    = !isInvalid;

      const endpoint = type === 'phone'
        ? `/clientes/${clientId}/phones/${id}/invalid`
        : `/clientes/${clientId}/emails/${id}/invalid`;

      try {
        const res = await fetch(endpoint, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            [csrfHeader]: csrfToken
          },
          body: 'invalid=' + encodeURIComponent(String(newVal))
        });
        if (!res.ok) throw new Error('HTTP ' + res.status);

        // Actualizar data-invalid
        btn.setAttribute('data-invalid', String(newVal));

        // Actualizar clases y texto del botón
        const span = btn.querySelector('span');
        if (newVal) {
          btn.classList.add('is-invalid');
          btn.title = 'Marcar como válido';
          if (span) span.textContent = 'Inválido';
        } else {
          btn.classList.remove('is-invalid');
          btn.title = 'Marcar como no válido';
          if (span) span.textContent = 'No válido';
        }

        // Actualizar color del contacto (el hermano anterior en .contact-item-row)
        const row = btn.closest('.contact-item-row');
        if (row) {
          const contact = row.querySelector('a, span:not(.btn-invalid-toggle span)');
          if (contact) {
            if (newVal) {
              contact.classList.add('contact-invalid');
            } else {
              contact.classList.remove('contact-invalid');
            }
          }
        }
      } catch (e) {
        alert('No se pudo actualizar: ' + e.message);
      }
    });
  });
  
  

  /* ══ TICKET SAVE ══ */
  document.querySelectorAll('.ticket-save').forEach(btn => {
    const feedback = btn.closest('td').querySelector('.ticket-feedback');
    if (feedback) feedback.style.display = 'none';
    btn.addEventListener('click', async function () {
      const clientId      = btn.getAttribute('data-client-id');
      const interactionId = btn.getAttribute('data-interaction-id');
      const input         = btn.closest('td').querySelector('.ticket-input');
      const fb            = btn.closest('td').querySelector('.ticket-feedback');
      try {
        const res = await fetch(`/clientes/${clientId}/interacciones/${interactionId}/ticket`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded', [csrfHeader]: csrfToken },
          body: 'ticketCode=' + encodeURIComponent(input.value)
        });
        if (!res.ok) throw new Error('HTTP ' + res.status);
        if (fb) { fb.style.display = 'inline'; setTimeout(() => fb.style.display = 'none', 2000); }
      } catch (e) { alert('No se pudo guardar el ticket.'); }
    });
  });

  /* ══ STATUS SELECT ══ */
  const statusMap = {
    'GRIS_SIN_CONTACTO'     : 'gris',
    'AZUL_VISITA_PROGRAMADA': 'azul-claro',
    'AZUL_VISITA_REALIZADA' : 'azul-oscuro',
    'NARANJA_QUIERE_VISITA' : 'naranja',
    'ROSA_DESCARTA'         : 'rosa',
    'VERDE_PENSANDO'        : 'verde',
    'AMARILLO_OFERTA'       : 'amarillo'
  };

  document.querySelectorAll('.status-select').forEach(sel => {
    const dot = sel.closest('td')?.querySelector('.status-dot');
    if (dot) {
      dot.className = 'dot status-dot';
      const cls = statusMap[dot.getAttribute('data-status')];
      if (cls) dot.classList.add(cls);
    }
    sel.addEventListener('change', async function () {
      const clientId      = sel.getAttribute('data-client-id');
      const interactionId = sel.getAttribute('data-interaction-id');
      const dotEl         = sel.closest('td').querySelector('.status-dot');
      try {
        const res = await fetch(`/clientes/${clientId}/interacciones/${interactionId}/status`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded', [csrfHeader]: csrfToken },
          body: 'status=' + encodeURIComponent(sel.value)
        });
        if (!res.ok) throw new Error('HTTP ' + res.status);
        if (dotEl) {
          dotEl.className = 'dot status-dot';
          const cls = statusMap[sel.value];
          if (cls) dotEl.classList.add(cls);
          dotEl.setAttribute('data-status', sel.value);
        }
      } catch (e) { alert('No se pudo actualizar el estado.'); }
    });
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
    _commentTargetSpan  = span;
    _commentClientId    = span.getAttribute('data-cid');
    _commentIid         = span.getAttribute('data-iid');
    const isPlaceholder = span.classList.contains('comment-empty');
    commentEditor.innerHTML = isPlaceholder ? '' : span.innerHTML.trim();
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
    if ((e.ctrlKey || e.altKey) && e.key === 'Enter') { e.preventDefault(); document.execCommand('insertLineBreak'); }
    if (e.ctrlKey && e.key === 'h') { e.preventDefault(); applyHighlight(); }
  });

  function applyHighlight() {
    const sel = window.getSelection();
    if (!sel || sel.isCollapsed) return;
    const range    = sel.getRangeAt(0);
    const ancestor = range.commonAncestorContainer;
    const markParent = ancestor.nodeType === 3
      ? ancestor.parentElement?.closest('mark')
      : ancestor.closest?.('mark');
    if (markParent) { markParent.replaceWith(...markParent.childNodes); return; }
    const mark = document.createElement('mark');
    try { range.surroundContents(mark); }
    catch(_) { const fragment = range.extractContents(); mark.appendChild(fragment); range.insertNode(mark); }
    sel.removeAllRanges();
  }

  document.getElementById('btnHighlight').addEventListener('click', () => { commentEditor.focus(); applyHighlight(); });

  document.getElementById('btnClearMark').addEventListener('click', () => {
    commentEditor.focus();
    const sel = window.getSelection();
    if (!sel || sel.isCollapsed) return;
    const range    = sel.getRangeAt(0);
    const fragment = range.extractContents();
    fragment.querySelectorAll('mark').forEach(m => m.replaceWith(...m.childNodes));
    range.insertNode(fragment);
    sel.removeAllRanges();
  });

  document.getElementById('btnClearAll').addEventListener('click', () => {
    commentEditor.innerHTML = '';
    commentEditor.focus();
  });

  document.getElementById('btnSaveComment').addEventListener('click', async function() {
    if (!_commentIid || !_commentClientId) return;
    const html = sanitizeHtml(commentEditor.innerHTML);
    try {
      const res = await fetch(`/clientes/${_commentClientId}/interacciones/${_commentIid}/comments`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded', [csrfHeader]: csrfToken },
        body: 'comments=' + encodeURIComponent(html)
      });
      if (!res.ok) throw new Error('HTTP ' + res.status);
      if (_commentTargetSpan) {
        if (!html || html.trim() === '' || html.trim() === '<br>') {
          _commentTargetSpan.innerHTML = '— <span>(editar)</span>';
          _commentTargetSpan.classList.add('comment-empty');
          _commentTargetSpan.classList.remove('comment-filled');
        } else {
          _commentTargetSpan.innerHTML = html;
          _commentTargetSpan.classList.remove('comment-empty');
          _commentTargetSpan.classList.add('comment-filled');
        }
      }
      closeCommentModal();
    } catch(err) {
      alert('Error al guardar el comentario.');
      console.error(err);
    }
  });

})();