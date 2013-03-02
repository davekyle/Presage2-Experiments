DELETE FROM agenttransient WHERE "simId" IN (SELECT id FROM simulations);
DELETE FROM agents WHERE "simId" IN (SELECT id FROM simulations);
DELETE FROM environmenttransient WHERE "simId" IN (SELECT id FROM simulations);
DELETE FROM environment WHERE "simId" IN (SELECT id FROM simulations);
DELETE FROM simulations WHERE "id" IN (SELECT id FROM simulations);