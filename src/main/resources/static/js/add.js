(function () {
  const comboInput    = document.getElementById('comboInput');
  const dropdown      = document.getElementById('comboDropdown');
  const chip          = document.getElementById('comboChip');
  const chipLabel     = document.getElementById('comboChipLabel');
  const comboClearBtn = document.getElementById('comboClearBtn');
  const wrapper       = document.getElementById('comboWrapper');

  let currentHighlight = -1;
  let visibleOptions   = [];

  const allItems = (CATALOG || [])
    .filter(p => !p.sold)
    .map(p => ({
      code         : p.propertyCode  || '',
      type         : p.propertyType  || '',
      address      : p.address       || '',
      municipality : p.municipality  || '',
      preVendido   : p.preVendido    || false,
      label        : [p.propertyCode, p.propertyType, p.municipality].filter(Boolean).join(' · ')
    }));

  function escHtml(str) {
    return (str||'').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
  }

  function renderDropdown(q) {
    dropdown.innerHTML = '';
    currentHighlight = -1;
    const lq = (q || '').trim().toLowerCase();
    visibleOptions = lq === ''
      ? allItems
      : allItems.filter(item =>
          item.code.toLowerCase().includes(lq) ||
          item.type.toLowerCase().includes(lq) ||
          item.municipality.toLowerCase().includes(lq) ||
          item.address.toLowerCase().includes(lq)
        );
    if (visibleOptions.length === 0) {
      dropdown.innerHTML = '<div class="combo-empty">Sin resultados</div>';
      return;
    }
    visibleOptions.forEach((item, idx) => {
      const div = document.createElement('div');
      div.className = 'combo-option';
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
  }

  function selectItem(item) {
    document.getElementById('propertyCode').value = item.code;
    document.getElementById('propertyType').value = item.type;
    document.getElementById('address').value      = item.address;
    document.getElementById('municipality').value = item.municipality;
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
    ['propertyCode','propertyType','address','municipality'].forEach(id => {
      document.getElementById(id).value = '';
    });
  }

  function openDropdown(q) { renderDropdown(q); dropdown.classList.add('open'); }
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
  comboClearBtn.addEventListener('click', clearSelection);
  document.addEventListener('click', e => { if (!wrapper.contains(e.target)) closeDropdown(); });
})();