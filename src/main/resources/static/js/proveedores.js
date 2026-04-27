(function () {
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
})();