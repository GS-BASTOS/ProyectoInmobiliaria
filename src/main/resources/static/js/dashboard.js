function getCsrf() {
  return {
    token:  document.querySelector('meta[name="_csrf"]').getAttribute('content'),
    header: document.querySelector('meta[name="_csrf_header"]').getAttribute('content')
  };
}

/* ══ MINI CALENDARIO ══ */
(function () {
  const today    = new Date(TODAY_STR + 'T00:00:00');
  const year     = today.getFullYear();
  const month    = today.getMonth();
  const days     = new Date(year, month + 1, 0).getDate();
  const firstDow = (new Date(year, month, 1).getDay() + 6) % 7;
  const months   = ['Enero','Febrero','Marzo','Abril','Mayo','Junio',
                    'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];

  document.getElementById('calMonthYear').textContent = months[month] + ' ' + year;
  const grid = document.getElementById('calGrid');

  ['L','M','X','J','V','S','D'].forEach(d => {
    const el = document.createElement('div');
    el.className = 'day-name'; el.textContent = d;
    grid.appendChild(el);
  });
  for (let i = 0; i < firstDow; i++) {
    const el = document.createElement('div');
    el.className = 'day empty'; grid.appendChild(el);
  }
  for (let d = 1; d <= days; d++) {
    const el = document.createElement('div');
    el.className = 'day' + (d === today.getDate() ? ' today' : '');
    el.textContent = d;
    grid.appendChild(el);
  }
})();

/* ══ GRÁFICO PIZZA ══ */
(function () {
  if (!TOP_PROPS.length) return;

  const labels = TOP_PROPS.map(p => p[0]);
  const data   = TOP_PROPS.map(p => p[1]);
  const COLORS = [
    '#2563eb','#3b82f6','#60a5fa','#93c5fd',
    '#1d4ed8','#6366f1','#8b5cf6','#a78bfa',
    '#06b6d4','#0ea5e9'
  ];

  const ctx = document.getElementById('chartInmuebles').getContext('2d');
  new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels,
      datasets: [{
        data,
        backgroundColor: data.map((_, i) => COLORS[i % COLORS.length]),
        borderWidth: 3,
        borderColor: '#ffffff',
        hoverOffset: 10
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: true,
      cutout: '58%',
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            label: ctx => ` ${ctx.label}: ${ctx.parsed} interacciones`
          }
        }
      }
    }
  });

  const legend = document.getElementById('chartLegend');
  labels.forEach((label, i) => {
    const item = document.createElement('div');
    item.className = 'chart-legend-item';
    item.innerHTML = `
      <div class="chart-legend-dot"
           style="background:${COLORS[i % COLORS.length]}"></div>
      <span class="chart-legend-label">${label}</span>
      <span class="chart-legend-val">${data[i]}</span>
    `;
    legend.appendChild(item);
  });
})();

/* ══ MODAL NOTA ══ */
function openNotaModal() {
  document.getElementById('notaFecha').value = new Date().toISOString().split('T')[0];
  document.getElementById('modalNota').classList.add('open');
}
document.getElementById('modalNota').addEventListener('click', function (e) {
  if (e.target === this) this.classList.remove('open');
});
const modalUsuario = document.getElementById('modalUsuario');
if (modalUsuario) {
  modalUsuario.addEventListener('click', function (e) {
    if (e.target === this) this.classList.remove('open');
  });
}

/* ══ ELIMINAR NOTA ══ */
async function deleteNota(btn) {
  if (!confirm('¿Eliminar esta nota?')) return;
  const csrf = getCsrf();
  const res  = await fetch('/agenda/' + btn.getAttribute('data-id') + '/eliminar', {
    method: 'POST',
    headers: { [csrf.header]: csrf.token }
  });
  if (res.ok) btn.closest('.note-item').remove();
}

/* ══ ELIMINAR USUARIO ══ */
async function deleteUser(btn) {
  if (!confirm('¿Eliminar este usuario? Esta acción no se puede deshacer.')) return;
  const csrf = getCsrf();
  const res  = await fetch('/usuarios/' + btn.getAttribute('data-id') + '/eliminar', {
    method: 'POST',
    headers: { [csrf.header]: csrf.token }
  });
  if (res.ok) btn.closest('.user-row').remove();
}