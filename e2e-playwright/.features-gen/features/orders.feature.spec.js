// Generated from: features\orders.feature
import { test } from "playwright-bdd";

test.describe('Liste des commandes', () => {

  test.beforeEach('Background', async ({ Given, page }, testInfo) => { if (testInfo.error) return;
    await Given('je suis sur la page des commandes', null, { page }); 
  });
  
  test('Affichage de la liste', async ({ Then, page }) => { 
    await Then('le tableau des commandes contient au moins 1 ligne', null, { page }); 
  });

  test.describe('Recherche par client', () => {

    test('Example #1', async ({ When, Then, page }) => { 
      await When('je recherche le client "Dupont"', null, { page }); 
      await Then('toutes les lignes affichées concernent le client "Dupont"', null, { page }); 
    });

    test('Example #2', async ({ When, Then, page }) => { 
      await When('je recherche le client "Martin"', null, { page }); 
      await Then('toutes les lignes affichées concernent le client "Martin"', null, { page }); 
    });

  });

  test('Recherche sans résultat', async ({ When, Then, page }) => { 
    await When('je recherche le client "ClientInconnu"', null, { page }); 
    await Then('le message "Aucun résultat" est affiché', null, { page }); 
  });

  test('Ouverture du détail', async ({ When, Then, page }) => { 
    await When('je clique sur la première commande', null, { page }); 
    await Then('la page de détail de la commande est affichée', null, { page }); 
  });

});

// == technical section ==

test.use({
  $test: [({}, use) => use(test), { scope: 'test', box: true }],
  $uri: [({}, use) => use('features\\orders.feature'), { scope: 'test', box: true }],
  $bddFileData: [({}, use) => use(bddFileData), { scope: "test", box: true }],
});

const bddFileData = [ // bdd-data-start
  {"pwTestLine":10,"pickleLine":9,"tags":[],"steps":[{"pwStepLine":7,"gherkinStepLine":7,"keywordType":"Context","textWithKeyword":"Given je suis sur la page des commandes","isBg":true,"stepMatchArguments":[]},{"pwStepLine":11,"gherkinStepLine":10,"keywordType":"Outcome","textWithKeyword":"Then le tableau des commandes contient au moins 1 ligne","stepMatchArguments":[{"group":{"start":43,"value":"1"},"parameterTypeName":"int"}]}]},
  {"pwTestLine":16,"pickleLine":18,"tags":[],"steps":[{"pwStepLine":7,"gherkinStepLine":7,"keywordType":"Context","textWithKeyword":"Given je suis sur la page des commandes","isBg":true,"stepMatchArguments":[]},{"pwStepLine":17,"gherkinStepLine":13,"keywordType":"Action","textWithKeyword":"When je recherche le client \"Dupont\"","stepMatchArguments":[{"group":{"start":23,"value":"\"Dupont\"","children":[{"start":24,"value":"Dupont","children":[{}]},{"children":[{}]}]},"parameterTypeName":"string"}]},{"pwStepLine":18,"gherkinStepLine":14,"keywordType":"Outcome","textWithKeyword":"Then toutes les lignes affichées concernent le client \"Dupont\"","stepMatchArguments":[{"group":{"start":49,"value":"\"Dupont\"","children":[{"start":50,"value":"Dupont","children":[{}]},{"children":[{}]}]},"parameterTypeName":"string"}]}]},
  {"pwTestLine":21,"pickleLine":19,"tags":[],"steps":[{"pwStepLine":7,"gherkinStepLine":7,"keywordType":"Context","textWithKeyword":"Given je suis sur la page des commandes","isBg":true,"stepMatchArguments":[]},{"pwStepLine":22,"gherkinStepLine":13,"keywordType":"Action","textWithKeyword":"When je recherche le client \"Martin\"","stepMatchArguments":[{"group":{"start":23,"value":"\"Martin\"","children":[{"start":24,"value":"Martin","children":[{}]},{"children":[{}]}]},"parameterTypeName":"string"}]},{"pwStepLine":23,"gherkinStepLine":14,"keywordType":"Outcome","textWithKeyword":"Then toutes les lignes affichées concernent le client \"Martin\"","stepMatchArguments":[{"group":{"start":49,"value":"\"Martin\"","children":[{"start":50,"value":"Martin","children":[{}]},{"children":[{}]}]},"parameterTypeName":"string"}]}]},
  {"pwTestLine":28,"pickleLine":21,"tags":[],"steps":[{"pwStepLine":7,"gherkinStepLine":7,"keywordType":"Context","textWithKeyword":"Given je suis sur la page des commandes","isBg":true,"stepMatchArguments":[]},{"pwStepLine":29,"gherkinStepLine":22,"keywordType":"Action","textWithKeyword":"When je recherche le client \"ClientInconnu\"","stepMatchArguments":[{"group":{"start":23,"value":"\"ClientInconnu\"","children":[{"start":24,"value":"ClientInconnu","children":[{}]},{"children":[{}]}]},"parameterTypeName":"string"}]},{"pwStepLine":30,"gherkinStepLine":23,"keywordType":"Outcome","textWithKeyword":"Then le message \"Aucun résultat\" est affiché","stepMatchArguments":[{"group":{"start":11,"value":"\"Aucun résultat\"","children":[{"start":12,"value":"Aucun résultat","children":[{}]},{"children":[{}]}]},"parameterTypeName":"string"}]}]},
  {"pwTestLine":33,"pickleLine":25,"tags":[],"steps":[{"pwStepLine":7,"gherkinStepLine":7,"keywordType":"Context","textWithKeyword":"Given je suis sur la page des commandes","isBg":true,"stepMatchArguments":[]},{"pwStepLine":34,"gherkinStepLine":26,"keywordType":"Action","textWithKeyword":"When je clique sur la première commande","stepMatchArguments":[]},{"pwStepLine":35,"gherkinStepLine":27,"keywordType":"Outcome","textWithKeyword":"Then la page de détail de la commande est affichée","stepMatchArguments":[]}]},
]; // bdd-data-end