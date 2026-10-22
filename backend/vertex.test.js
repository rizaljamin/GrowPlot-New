import test from 'node:test';
import assert from 'node:assert/strict';
import {vertexCheck} from './vertex.js';

test('Vertex provider reports disabled when the backend flag is off', async () => {
  const previous = process.env.GROWPLOT_VERTEX_ENABLED;
  delete process.env.GROWPLOT_VERTEX_ENABLED;
  try {
    const response = await vertexCheck([]);
    assert.equal(response.provider, 'Gemini on Agent Platform');
    assert.equal(response.status, 'not_configured');
  } finally {
    if (previous === undefined) delete process.env.GROWPLOT_VERTEX_ENABLED;
    else process.env.GROWPLOT_VERTEX_ENABLED = previous;
  }
});

test('Vertex provider authenticates with Cloud Run metadata and returns visible-analysis candidates', async () => {
  const previousFetch = globalThis.fetch;
  const previous = {
    enabled: process.env.GROWPLOT_VERTEX_ENABLED,
    project: process.env.GOOGLE_CLOUD_PROJECT,
    location: process.env.GOOGLE_CLOUD_LOCATION,
    model: process.env.VERTEX_MODEL_ID
  };
  process.env.GROWPLOT_VERTEX_ENABLED = 'true';
  process.env.GOOGLE_CLOUD_PROJECT = 'growplot';
  process.env.GOOGLE_CLOUD_LOCATION = 'global';
  process.env.VERTEX_MODEL_ID = 'gemini-3.5-flash';

  let requestBody;
  globalThis.fetch = async (url, options = {}) => {
    if (String(url).includes('metadata.google.internal')) {
      assert.equal(options.headers['Metadata-Flavor'], 'Google');
      return Response.json({ access_token: 'unit-test-token' });
    }
    assert.match(String(url), /projects\/growplot\/locations\/global\/publishers\/google\/models\/gemini-3\.5-flash:generateContent$/);
    assert.equal(options.headers.Authorization, 'Bearer unit-test-token');
    requestBody = JSON.parse(options.body);
    return Response.json({
      candidates: [{
        content: {
          parts: [{
            text: JSON.stringify({
              summary: 'Several leaves show dry brown edges.',
              speciesCandidates: [{ commonName: 'Example plant', scientificName: 'Example species', confidence: 0.62 }],
              visibleFindings: ['Yellowing on older leaves.'],
              possibleCauses: ['Water stress.'],
              nextChecks: ['Check soil moisture.'],
              limitations: ['Image alone cannot confirm the cause.']
            })
          }]
        }
      }]
    });
  };

  try {
    const image = { mimetype: 'image/jpeg', buffer: Buffer.from('photo-bytes') };
    const response = await vertexCheck([image], { species: 'unconfirmed', symptoms: 'leaf edges brown' });
    assert.equal(response.status, 'ok');
    assert.equal(response.model, 'gemini-3.5-flash');
    assert.equal(response.imageCountSubmitted, 1);
    assert.ok(response.candidates.some(item => item.type === 'visual summary'));
    assert.ok(requestBody.contents[0].parts.some(part => part.inlineData?.data === image.buffer.toString('base64')));
    assert.match(requestBody.contents[0].parts.at(-1).text, /Do not claim to have computed HSV values or YOLO detections/);
  } finally {
    globalThis.fetch = previousFetch;
    for (const [key, value] of Object.entries({
      GROWPLOT_VERTEX_ENABLED: previous.enabled,
      GOOGLE_CLOUD_PROJECT: previous.project,
      GOOGLE_CLOUD_LOCATION: previous.location,
      VERTEX_MODEL_ID: previous.model
    })) {
      if (value === undefined) delete process.env[key];
      else process.env[key] = value;
    }
  }
});
