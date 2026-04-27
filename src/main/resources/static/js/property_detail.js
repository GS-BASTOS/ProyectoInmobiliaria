(function () {

  /* ── TABS ── */
  const tabBtns  = document.querySelectorAll('.tab-btn');
  const tabPanes = document.querySelectorAll('.tab-pane');

  tabBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      tabBtns.forEach(b => b.classList.remove('active'));
      tabPanes.forEach(p => p.classList.remove('active'));
      btn.classList.add('active');
      document.getElementById('tab-' + btn.dataset.tab).classList.add('active');
    });
  });

  /* ── TOGGLE PUBLICAR ── */
  const toggleEl    = document.getElementById('togglePublicar');
  const publishWrap = document.getElementById('publishWrap');
  const publishLbl  = document.getElementById('publishLabel');
  const publishSub  = document.getElementById('publishSub');
  const publishStat = document.getElementById('publishStatus');
  const propertyId  = document.body.dataset.propertyId;
  const csrfToken   = document.body.dataset.csrf;

  if (toggleEl) {
    toggleEl.addEventListener('change', () => {
      const checked = toggleEl.checked;
      publishStat.textContent = 'Guardando...';
      fetch('/inmuebles/' + propertyId + '/publicar', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
          'X-CSRF-TOKEN': csrfToken
        },
        body: 'publicado=' + checked
      })
      .then(r => {
        if (!r.ok) throw new Error('Error ' + r.status);
        publishLbl.textContent = checked ? 'Publicado en web' : 'No publicado';
        publishSub.textContent = checked ? 'Visible en el catálogo público.' : 'Solo para el equipo interno.';
        publishWrap.classList.toggle('is-published', checked);
        publishStat.textContent = checked ? '✓ Publicado correctamente' : '✓ Ocultado del catálogo';
        setTimeout(() => { publishStat.textContent = ''; }, 2500);
      })
      .catch(() => {
        publishStat.textContent = '⚠ Error al guardar. Inténtalo de nuevo.';
        toggleEl.checked = !checked;
      });
    });
  }

  /* ── LIGHTBOX ── */
  const lightbox      = document.getElementById('lightbox');
  const lightboxImg   = document.getElementById('lightboxImg');
  const lightboxClose = document.getElementById('lightboxClose');

  document.querySelectorAll('.lightbox-trigger').forEach(img => {
    img.addEventListener('click', () => {
      lightboxImg.src = img.dataset.src || img.src;
      lightbox.classList.add('open');
    });
  });

  if (lightboxClose) {
    lightboxClose.addEventListener('click', () => lightbox.classList.remove('open'));
  }
  if (lightbox) {
    lightbox.addEventListener('click', e => {
      if (e.target === lightbox) lightbox.classList.remove('open');
    });
  }
  document.addEventListener('keydown', e => {
    if (e.key === 'Escape') lightbox.classList.remove('open');
  });

  /* ── UPLOAD ZONE ── */
  const uploadZone = document.getElementById('uploadZone');
  const fileInput  = document.getElementById('fileInput');
  const fileNames  = document.getElementById('fileNames');
  const uploadBtn  = document.getElementById('uploadBtn');

  if (fileInput) {
    fileInput.addEventListener('change', () => {
      const files = Array.from(fileInput.files);
      if (files.length) {
        fileNames.textContent = files.map(f => f.name).join(', ');
        uploadBtn.disabled = false;
        uploadBtn.style.opacity = '1';
      } else {
        fileNames.textContent = '';
        uploadBtn.disabled = true;
        uploadBtn.style.opacity = '0.5';
      }
    });
  }

  if (uploadZone) {
    uploadZone.addEventListener('dragover',  e => { e.preventDefault(); uploadZone.classList.add('dragover'); });
    uploadZone.addEventListener('dragleave', () => uploadZone.classList.remove('dragover'));
    uploadZone.addEventListener('drop', e => {
      e.preventDefault();
      uploadZone.classList.remove('dragover');
      fileInput.files = e.dataTransfer.files;
      fileInput.dispatchEvent(new Event('change'));
    });
  }

})();