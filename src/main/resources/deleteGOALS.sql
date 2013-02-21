DELETE FROM agenttransient WHERE "simId" IN (SELECT id FROM simulations WHERE (parameters->'ownChoiceMethod' = 'GOALS'));
DELETE FROM agents WHERE "simId" IN (SELECT id FROM simulations WHERE (parameters->'ownChoiceMethod' = 'GOALS'));
DELETE FROM environmenttransient WHERE "simId" IN (SELECT id FROM simulations WHERE (parameters->'ownChoiceMethod' = 'GOALS'));
DELETE FROM environment WHERE "simId" IN (SELECT id FROM simulations WHERE (parameters->'ownChoiceMethod' = 'GOALS'));
DELETE FROM simulations WHERE "id" IN (SELECT id FROM simulations WHERE (parameters->'ownChoiceMethod' = 'GOALS'));