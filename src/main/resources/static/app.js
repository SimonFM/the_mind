let currentUser = null;
let currentGame = null;

$(document).ready(() => {
  setupClient();
});


$(() => {
  $('form').on('submit', e => e.preventDefault());
  $('#connect').click(() => { connect(); });
  $('#disconnect').click(() => { disconnect(); });
  $('#join-room').click(() => { joinRoom(); });
  $('#create-room').click(() => { createRoom(); });
  $('#get-room').click(() => { getRoom(); });
});

const setConnected = (connected) => {
  $('#connect').prop('disabled', connected);
  $('#disconnect').prop('disabled', !connected);
  $('#join-room-form').prop('disabled', !connected);
  $('#create-room-form').prop('disabled', !connected);
  $('#get-room-form').prop('disabled', !connected);
  connected ? $('#conversation').show() : $('#conversation').hide();
  $('#the-mind-messages').html('');
}


const disconnect = () => {
  if (stompClient !== null) {
    stompClient.disconnect();
  }
  setConnected(false);
  sendMessage('/app/game/disconnect', { 'userId': currentUser && currentUser.id })
}

const joinRoom = () => {
  sendMessage('/app/game/join', { 'invite': $('#join-code').val(), 'userId': currentUser && currentUser.id || '' })
}

const createRoom = () => {
  sendMessage('/app/game/create', { 'name': $('#room-key').val() })
}

const getRoom = () => {
  sendMessage('/app/game/get', { 'invite': $('#room-key').val() })
}

const addMessage = (message) => {
  console.log(message);
  $('#the-mind-messages').append(`<tr><td>${message}</td></tr>`);
}

// Web socket things
let stompClient = null;

const setupClient = () => {
  let socket = new SockJS('/mindfulness/ws');
  stompClient = Stomp.over(socket);
  stompClient.connect({}, (frame) => {
    setConnected(true);
    console.log('Connected: ' + frame);
    stompClient.subscribe('/user/topic/game/connected', (message) => {
      if (!message || !message.body) {
        return;
      }
      currentUser = JSON.parse(message.body);
      addMessage(JSON.stringify(message));
      $('#user').html(`User: ${currentUser.id}`);
    });
    stompClient.subscribe('/user/topic/game/disconnected', (message) => {
      currentUser = null;
      addMessage(JSON.stringify(message));
    });
    stompClient.subscribe('/user/topic/game/join', (message) => {
      addMessage(message);
      if (!message || !message.body) {
        return;
      }
      currentGame = JSON.parse(message.body);
      stompClient.subscribe(`/user/topic/game/${currentGame.id}`, (message) => {
        addMessage(message);
      });
    });
    stompClient.subscribe('/user/topic/game/create', (message) => {
      addMessage(message);
      if (!message || !message.body) {
        return;
      }
      currentGame = JSON.parse(message.body);
      $('#room-name').html(`Room: ${currentGame.name}`);
      stompClient.subscribe(`/user/topic/game/${currentGame.id}`, (message) => {
        addMessage(message);
      });
    });
    stompClient.subscribe('/user/topic/game/start', (message) => {
      addMessage(message);
      if (!message || !message.body) {
        return;
      }
      currentGame = JSON.parse(message.body);
      stompClient.subscribe(`/user/topic/game/${currentGame.id}`, (message) => {
        addMessage(message);
      });
    });
    sendMessage('/app/game/connect')
  });
}

const sendMessage = (url, payload = {}) => {
  stompClient.send(url, {}, JSON.stringify(payload));
}