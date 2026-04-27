(function () {
  const csrfToken  = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

  /* ── Toggle web pública ── */
  document.querySelectorAll('.toggle-publicado').forEach(chk => {
    chk.addEventListener('change', async function () {
      const id    = chk.getAttribute('data-id');
      const label = document.querySelector(`[data-label-id="${id}"]`);
      chk.disabled = true;
      try {
        const res  = await fetch(`/inmuebles/${id}/publicar`, {
          method: 'POST',
          headers: { [csrfHeader]: csrfToken }
        });
        const data = await res.json();
        if (label) {
          label.textContent = data.publicado ? 'Sí' : 'No';
          label.classList.toggle('is-on', data.publicado);
        }
        chk.checked = data.publicado;
      } catch (e) {
        chk.checked = !chk.checked;
        alert('No se pudo actualizar la publicación.');
      } finally {
        chk.disabled = false;
      }
    });
  });

  /* ── Vendido ── */
  document.querySelectorAll('.btn-vendido').forEach(btn => {
    btn.addEventListener('click', async function () {
      const id   = btn.getAttribute('data-id');
      const sold = btn.getAttribute('data-sold') === 'true';
      try {
        const res = await fetch(`/inmuebles/${id}/vendido`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded', [csrfHeader]: csrfToken },
          body: 'sold=' + encodeURIComponent(String(!sold))
        });
        if (!res.ok) throw new Error();
        window.location.reload();
      } catch { alert('No se pudo actualizar el estado.'); }
    });
  });

  /* ── Pre vendido ── */
  document.querySelectorAll('.btn-prevendido').forEach(btn => {
    btn.addEventListener('click', async function () {
      const id         = btn.getAttribute('data-id');
      const preVendido = btn.getAttribute('data-prevendido') === 'true';
      try {
        const res = await fetch(`/inmuebles/${id}/prevendido`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded', [csrfHeader]: csrfToken },
          body: 'preVendido=' + encodeURIComponent(String(!preVendido))
        });
        if (!res.ok) throw new Error();
        window.location.reload();
      } catch { alert('No se pudo actualizar el estado.'); }
    });
  });

  /* ── Modal eliminar ── */
  const backdrop   = document.getElementById('deleteModalBackdrop');
  const modalCode  = document.getElementById('deleteModalCode');
  const modalCount = document.getElementById('deleteModalCount');
  const modalWarn  = document.getElementById('deleteModalWarning');
  const modalForm  = document.getElementById('deleteModalForm');
  const btnCancel  = document.getElementById('btnCancelDelete');

  document.querySelectorAll('.btn-delete').forEach(btn => {
    btn.addEventListener('click', function () {
      const id    = btn.getAttribute('data-id');
      const code  = btn.getAttribute('data-code');
      const count = parseInt(btn.getAttribute('data-count'), 10);
      modalCode.textContent = code;
      modalForm.action      = `/inmuebles/${id}/eliminar`;
      if (count > 0) { modalCount.textContent = count; modalWarn.classList.add('show'); }
      else            { modalWarn.classList.remove('show'); }
      backdrop.classList.add('open');
    });
  });

  btnCancel.addEventListener('click', () => backdrop.classList.remove('open'));
  backdrop.addEventListener('click', e => {
    if (e.target === backdrop) backdrop.classList.remove('open');
  });
})();