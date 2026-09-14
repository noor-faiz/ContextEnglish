(function () {
  const container = document.getElementById('targetItemsContainer');
  const itemTemplate = document.getElementById('targetItemTemplate');
  const answerRowTemplate = document.getElementById('answerRowTemplate');
  const distractorRowTemplate = document.getElementById('distractorRowTemplate');
  const formError = document.getElementById('formError');

  function addAnswerRow(answersContainer, data) {
    const node = answerRowTemplate.content.cloneNode(true);
    const row = node.querySelector('[data-answer-row]');
    if (data) {
      row.querySelector('[data-answer-text]').value = data.answerText || '';
      row.querySelector('[data-answer-type]').value = data.answerType || 'EXACT';
    }
    row.querySelector('[data-remove-row]').addEventListener('click', () => row.remove());
    answersContainer.appendChild(row);
  }

  function addDistractorRow(distractorsContainer, text) {
    const node = distractorRowTemplate.content.cloneNode(true);
    const row = node.querySelector('[data-distractor-row]');
    if (text) row.querySelector('[data-distractor-text]').value = text;
    row.querySelector('[data-remove-row]').addEventListener('click', () => row.remove());
    distractorsContainer.appendChild(row);
  }

  function addTargetItemBlock(itemData) {
    const node = itemTemplate.content.cloneNode(true);
    const block = node.querySelector('[data-block]');

    if (itemData) {
      block.querySelector('[data-field="surfaceText"]').value = itemData.surfaceText || '';
      block.querySelector('[data-field="itemType"]').value = itemData.itemType || 'WORD';
      block.querySelector('[data-field="answerMode"]').value = itemData.answerMode || 'MCQ';
      block.querySelector('[data-field="hintText"]').value = itemData.hintText || '';
      if (itemData.explanation) {
        block.querySelector('[data-field="explanationText"]').value = itemData.explanation.explanationText || '';
        block.querySelector('[data-field="explanationNative"]').value = itemData.explanation.explanationNative || '';
      }
    }

    const answersContainer = block.querySelector('[data-answers-container]');
    const distractorsContainer = block.querySelector('[data-distractors-container]');

    if (itemData && itemData.acceptableAnswers && itemData.acceptableAnswers.length) {
      itemData.acceptableAnswers.forEach((a) => addAnswerRow(answersContainer, a));
    } else {
      addAnswerRow(answersContainer, null);
    }

    if (itemData && itemData.distractorOptions && itemData.distractorOptions.length) {
      itemData.distractorOptions.forEach((d) => addDistractorRow(distractorsContainer, d.optionText));
    }

    block.querySelector('[data-add-answer]').addEventListener('click', () => addAnswerRow(answersContainer, null));
    block.querySelector('[data-add-distractor]').addEventListener('click', () => addDistractorRow(distractorsContainer, null));
    block.querySelector('[data-remove-item]').addEventListener('click', () => block.remove());

    container.appendChild(block);
  }

  document.getElementById('addTargetItemBtn').addEventListener('click', () => addTargetItemBlock(null));

  // ---- Populate from existing passage (edit mode) ----
  const existing = window.EXISTING_PASSAGE;
  if (existing) {
    document.getElementById('title').value = existing.title || '';
    document.getElementById('contentText').value = existing.contentText || '';
    document.getElementById('level').value = existing.level || 'BEGINNER';
    document.getElementById('contentType').value = existing.contentType || 'STORY';
    document.getElementById('topic').value = existing.topic || '';
    (existing.targetItems || []).forEach((ti) => addTargetItemBlock(ti));
  }
  if (container.children.length === 0) {
    addTargetItemBlock(null); // start with one empty block for a brand-new passage
  }

  // ---- Gather + save ----
  function gatherPayload() {
    const targetItems = [];
    container.querySelectorAll('[data-block]').forEach((block) => {
      const acceptableAnswers = [];
      block.querySelectorAll('[data-answer-row]').forEach((row) => {
        const text = row.querySelector('[data-answer-text]').value.trim();
        if (!text) return;
        acceptableAnswers.push({
          answerText: text,
          answerType: row.querySelector('[data-answer-type]').value,
          required: true,
          pointsWeight: 1.0
        });
      });

      const distractorOptions = [];
      block.querySelectorAll('[data-distractor-row]').forEach((row) => {
        const text = row.querySelector('[data-distractor-text]').value.trim();
        if (text) distractorOptions.push(text);
      });

      targetItems.push({
        surfaceText: block.querySelector('[data-field="surfaceText"]').value.trim(),
        itemType: block.querySelector('[data-field="itemType"]').value,
        answerMode: block.querySelector('[data-field="answerMode"]').value,
        hintText: block.querySelector('[data-field="hintText"]').value.trim() || null,
        acceptableAnswers: acceptableAnswers,
        distractorOptions: distractorOptions,
        explanationText: block.querySelector('[data-field="explanationText"]').value.trim() || null,
        explanationNative: block.querySelector('[data-field="explanationNative"]').value.trim() || null
      });
    });

    return {
      title: document.getElementById('title').value.trim(),
      contentText: document.getElementById('contentText').value.trim(),
      level: document.getElementById('level').value,
      contentType: document.getElementById('contentType').value,
      topic: document.getElementById('topic').value.trim() || null,
      targetItems: targetItems
    };
  }

  document.getElementById('savePassageBtn').addEventListener('click', async () => {
    formError.style.display = 'none';
    const payload = gatherPayload();

    if (!payload.title || !payload.contentText) {
      formError.textContent = 'Title and passage text are required.';
      formError.style.display = 'block';
      return;
    }
    for (const ti of payload.targetItems) {
      if (ti.surfaceText && !payload.contentText.toLowerCase().includes(ti.surfaceText.toLowerCase())) {
        formError.textContent = 'Target word "' + ti.surfaceText + '" was not found in the passage text (check spelling/case).';
        formError.style.display = 'block';
        return;
      }
    }

    const url = window.PASSAGE_ID ? '/api/admin/passages/' + window.PASSAGE_ID : '/api/admin/passages';
    const method = window.PASSAGE_ID ? 'PUT' : 'POST';

    try {
      const res = await fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (!res.ok) {
        const errBody = await res.json().catch(() => ({}));
        throw new Error(errBody.error || 'Save failed');
      }
      window.location.href = '/admin/content';
    } catch (err) {
      formError.textContent = 'Could not save: ' + err.message;
      formError.style.display = 'block';
    }
  });
})();
