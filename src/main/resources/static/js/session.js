(function () {
  const state = window.CONTEXT_ENGLISH || { sessionAttemptId: 0, targetItems: [] };
  const itemsById = new Map(state.targetItems.map((item) => [String(item.id), item]));
  const answeredResults = new Map(); // itemId -> result payload
  let currentItemId = null;
  let hintUsed = false;

  const backdrop = document.getElementById('answerBackdrop');
  const panel = document.getElementById('answerPanel');
  const itemTypeTag = document.getElementById('answerItemType');
  const wordTitle = document.getElementById('answerWordTitle');
  const widgetArea = document.getElementById('answerWidgetArea');
  const hintToggle = document.getElementById('hintToggle');
  const hintTextEl = document.getElementById('hintText');
  const submitBtn = document.getElementById('submitAnswerBtn');
  const closeBtn = document.getElementById('closeAnswerBtn');
  const feedbackBox = document.getElementById('feedbackBox');
  const feedbackTitle = document.getElementById('feedbackTitle');
  const feedbackExplanation = document.getElementById('feedbackExplanation');
  const feedbackNative = document.getElementById('feedbackNative');
  const answeredCountEl = document.getElementById('answeredCount');
  const progressFill = document.getElementById('sessionProgressFill');
  const finishBtn = document.getElementById('finishSessionBtn');

  function totalItems() {
    return state.targetItems.length;
  }

  function updateProgress() {
    const answered = answeredResults.size;
    const total = totalItems();
    answeredCountEl.textContent = String(answered);
    progressFill.style.width = total === 0 ? '0%' : Math.round((answered / total) * 100) + '%';
    finishBtn.disabled = answered < total;
  }

  function statusClass(status) {
    if (status === 'FULLY_CORRECT') return 'answered-correct';
    if (status === 'PARTIALLY_CORRECT') return 'answered-partial';
    return 'answered-incorrect';
  }

  function markWordSpan(itemId, status) {
    document.querySelectorAll('.target-word[data-item-id="' + itemId + '"]').forEach((el) => {
      el.classList.remove('answered-correct', 'answered-partial', 'answered-incorrect');
      el.classList.add(statusClass(status));
    });
  }

  function renderWidget(item) {
    widgetArea.innerHTML = '';

    if (item.answerMode === 'FREE_TEXT') {
      const input = document.createElement('input');
      input.type = 'text';
      input.id = 'freeTextInput';
      input.placeholder = 'Type what you think it means...';
      widgetArea.appendChild(input);
      return;
    }

    const inputType = item.answerMode === 'MULTI_SELECT' ? 'checkbox' : 'radio';
    (item.options || []).forEach((opt, idx) => {
      const row = document.createElement('label');
      row.className = 'option-choice';
      row.innerHTML =
        '<input type="' + inputType + '" name="optionChoice" value="' + opt.key + '"/>' +
        '<span>' + opt.text + '</span>';
      const inputEl = row.querySelector('input');
      inputEl.addEventListener('change', () => {
        widgetArea.querySelectorAll('.option-choice').forEach((r) => r.classList.remove('selected'));
        if (inputType === 'radio') {
          row.classList.add('selected');
        } else {
          widgetArea.querySelectorAll('input:checked').forEach((c) => c.closest('.option-choice').classList.add('selected'));
        }
      });
      widgetArea.appendChild(row);
    });
  }

  function renderReadOnlyFeedback(item, result) {
    widgetArea.innerHTML = '<p class="text-muted">You already answered this one.</p>';
    showFeedback(result);
    submitBtn.style.display = 'none';
  }

  function showFeedback(result) {
    feedbackBox.className = 'feedback-box show';
    if (result.status === 'FULLY_CORRECT') {
      feedbackBox.classList.add('correct');
      feedbackTitle.textContent = '✅ Correct! (+' + Math.round(result.scoreAwarded) + ' pts)';
    } else if (result.status === 'PARTIALLY_CORRECT') {
      feedbackBox.classList.add('partial');
      feedbackTitle.textContent = '🟡 Partially correct (+' + Math.round(result.scoreAwarded) + ' pts)';
    } else {
      feedbackBox.classList.add('incorrect');
      feedbackTitle.textContent = '❌ Not quite';
    }
    feedbackExplanation.textContent = result.explanationText
      ? result.explanationText
      : ('The accepted answer(s): ' + result.correctAnswerDisplay);
    feedbackNative.textContent = result.explanationNative || '';
  }

  function openPanel(itemId) {
    const item = itemsById.get(String(itemId));
    if (!item) return;
    currentItemId = itemId;
    hintUsed = false;

    itemTypeTag.textContent = item.itemType;
    wordTitle.textContent = item.surfaceText;
    feedbackBox.className = 'feedback-box';
    submitBtn.style.display = 'inline-flex';
    submitBtn.disabled = false;

    if (item.hintText) {
      hintToggle.style.display = 'block';
      hintTextEl.style.display = 'none';
      hintTextEl.textContent = item.hintText;
      hintToggle.textContent = 'Show a hint';
    } else {
      hintToggle.style.display = 'none';
      hintTextEl.style.display = 'none';
    }

    if (answeredResults.has(String(itemId))) {
      renderReadOnlyFeedback(item, answeredResults.get(String(itemId)));
    } else {
      renderWidget(item);
    }

    backdrop.classList.add('open');
  }

  function closePanel() {
    backdrop.classList.remove('open');
    currentItemId = null;
  }

  function gatherPayload(item) {
    const payload = {
      targetItemId: item.id,
      sessionAttemptId: state.sessionAttemptId,
      hintUsed: hintUsed
    };
    if (item.answerMode === 'FREE_TEXT') {
      payload.freeText = (document.getElementById('freeTextInput') || {}).value || '';
    } else if (item.answerMode === 'MULTI_SELECT') {
      payload.selectedOptionKeys = Array.from(widgetArea.querySelectorAll('input:checked')).map((el) => el.value);
    } else {
      const checked = widgetArea.querySelector('input:checked');
      payload.selectedOptionKey = checked ? checked.value : null;
    }
    return payload;
  }

  async function submitAnswer() {
    const item = itemsById.get(String(currentItemId));
    if (!item) return;
    submitBtn.disabled = true;

    try {
      const res = await fetch('/api/answers/submit', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(gatherPayload(item))
      });
      if (!res.ok) throw new Error('Request failed');
      const result = await res.json();

      answeredResults.set(String(item.id), result);
      markWordSpan(item.id, result.status);
      showFeedback(result);
      submitBtn.style.display = 'none';
      updateProgress();
    } catch (err) {
      feedbackBox.className = 'feedback-box show incorrect';
      feedbackTitle.textContent = 'Something went wrong submitting that answer.';
      feedbackExplanation.textContent = 'Please try again.';
      submitBtn.disabled = false;
    }
  }

  document.querySelectorAll('.target-word[data-item-id]').forEach((el) => {
    el.addEventListener('click', () => openPanel(el.getAttribute('data-item-id')));
  });

  hintToggle.addEventListener('click', () => {
    hintUsed = true;
    hintTextEl.style.display = 'block';
  });
  submitBtn.addEventListener('click', submitAnswer);
  closeBtn.addEventListener('click', closePanel);
  backdrop.addEventListener('click', (e) => { if (e.target === backdrop) closePanel(); });

  updateProgress();
})();
