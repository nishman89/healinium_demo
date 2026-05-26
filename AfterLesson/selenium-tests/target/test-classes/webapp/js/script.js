'use strict';

const VALID_USERNAME = 'sparta';
const VALID_PASSWORD = 'correct';

const loginForm      = document.getElementById('login-form');
const usernameInput  = document.getElementById('user-name');
const passwordInput  = document.getElementById('password');
const successOverlay = document.getElementById('success-message');
const errorOverlay   = document.getElementById('error-message');
const closeSuccess   = document.getElementById('close-success');
const closeError     = document.getElementById('close-error');

loginForm.addEventListener('submit', function (event) {
  event.preventDefault();
  hideAll();

  const username = usernameInput.value.trim();
  const password = passwordInput.value;

  if (username === VALID_USERNAME && password === VALID_PASSWORD) {
    show(successOverlay);
  } else {
    show(errorOverlay);
  }
});

closeSuccess.addEventListener('click', function () {
  hide(successOverlay);
});

closeError.addEventListener('click', function () {
  hide(errorOverlay);
});

successOverlay.addEventListener('click', function (event) {
  if (event.target === successOverlay) {
    hide(successOverlay);
  }
});

errorOverlay.addEventListener('click', function (event) {
  if (event.target === errorOverlay) {
    hide(errorOverlay);
  }
});

document.addEventListener('keydown', function (event) {
  if (event.key === 'Escape') {
    hideAll();
  }
});

function show(element) {
  element.classList.remove('hidden');
}

function hide(element) {
  element.classList.add('hidden');
}

function hideAll() {
  hide(successOverlay);
  hide(errorOverlay);
}
