(function () {
  const csrfToken  = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

  /* ── Toast ── */
  const toast = document.getElementById('copyToast');
  let toastTimer;
  function showToast(msg) {
    toast.textContent = msg + ' ✓';
    toast.classList.add('show');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => toast.classList.remove('show'), 1800);
  }
  document.addEventListener('click', async function (e) {
    if (e.target.tagName === 'INPUT') return;
    if (e.target.closest('.wa-link')) return;
    if (e.target.closest('.precio-display')) return;
    const el = e.target.closest('.copyable');
    if (!el) return;
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

  /* ── Toggle form añadir ── */
  const btnShow   = document.getElementById('btnShowAddForm');
  const btnCancel = document.getElementById('btnCancelAddForm');
  const formWrap  = document.getElementById('addPropertyForm');

  btnShow.addEventListener('click', () => {
    formWrap.style.display = 'block';
    btnShow.style.display  = 'none';
  });
  btnCancel.addEventListener('click', () => {
    formWrap.style.display = 'none';
    btnShow.style.display  = 'inline-flex';
  });

  /* ── Precio inline ── */
  document.querySelectorAll('.precio-display').forEach(display => {
    display.addEventListener('click', () => {
      const form = display.nextElementSibling;
      display.style.display = 'none';
      form.style.display    = 'inline-flex';
      form.querySelector('input[type="number"]').focus();
    });
  });
  document.querySelectorAll('.btn-cancel-precio').forEach(btn => {
    btn.addEventListener('click', () => {
      const form    = btn.closest('.precio-form');
      const display = form.previousElementSibling;
      form.style.display    = 'none';
      display.style.display = 'inline-flex';
    });
  });
})();