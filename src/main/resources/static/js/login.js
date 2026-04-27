(function () {
  const toggleBtn = document.getElementById('togglePass');
  const passInput = document.getElementById('passwordInput');
  const eyeOpen   = document.getElementById('eyeOpen');
  const eyeClosed = document.getElementById('eyeClosed');

  toggleBtn.addEventListener('click', function () {
    const isHidden      = passInput.type === 'password';
    passInput.type      = isHidden ? 'text'  : 'password';
    eyeOpen.style.display   = isHidden ? 'none'  : 'block';
    eyeClosed.style.display = isHidden ? 'block' : 'none';
  });
})();