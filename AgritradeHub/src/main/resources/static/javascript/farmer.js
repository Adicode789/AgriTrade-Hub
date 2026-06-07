(function () {
  const form = document.querySelector('.needs-validation');
  const phone = document.getElementById('phone');

  // Allow only digits and limit to 10 characters
  phone.addEventListener('input', () => {
    phone.value = phone.value.replace(/\D/g, '').slice(0, 10);
  });

  // Bootstrap-style validation
  form.addEventListener('submit', function (e) {
    if (!form.checkValidity()) {
      e.preventDefault();
      e.stopPropagation();
    }
    form.classList.add('was-validated');
  }, false);
})();