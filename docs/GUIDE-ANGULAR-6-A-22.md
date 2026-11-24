# 🅰️ D'Angular 6 à Angular 22

Angular 6 date de 2018. Depuis, le framework a changé en profondeur. Voici l'essentiel, avec un « avant / après ».

## 1. Plus besoin de `NgModule` : les composants *standalone*
```ts
// Angular 6 : chaque composant devait être déclaré dans un module
@NgModule({ declarations: [AccountListComponent], imports: [CommonModule, HttpClientModule] })
export class AccountsModule {}

// Angular 22 : le composant est autonome (standalone par défaut)
@Component({
  selector: 'app-account-list',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './account-list.html',
})
export class AccountList {}
```
Le démarrage se fait dans `main.ts` avec `bootstrapApplication(App, appConfig)`, et `provideHttpClient()` / `provideRouter(routes)` sont déclarés dans `app.config.ts`.

## 2. L'injection avec `inject()`
```ts
// Avant
constructor(private http: HttpClient) {}
// Maintenant (style recommandé)
private readonly http = inject(HttpClient);
```

## 3. Les *signals* : la nouvelle réactivité
```ts
solde = signal(0);                                   // valeur réactive
soldeAffiche = computed(() => `${this.solde()} €`);  // dérivée, recalculée automatiquement
effect(() => console.log('Nouveau solde', this.solde()));

this.solde.set(150);
this.solde.update(s => s - 20);
```
- Dans le template : `{{ soldeAffiche() }}` (avec des parenthèses).
- RxJS reste utile pour les flux complexes. On passe de l'un à l'autre avec `toSignal()` / `toObservable()`.
- Entrées et sorties : `montant = input.required<number>()`, `valide = output<Virement>()`, `model()` pour le *two-way binding*.

## 4. Le nouveau *control flow* dans les templates
```html
<!-- Angular 6 -->
<div *ngIf="comptes.length > 0; else vide">
  <div *ngFor="let c of comptes; trackBy: trackById">{{ c.iban }}</div>
</div>

<!-- Angular 22 -->
@if (comptes().length > 0) {
  @for (c of comptes(); track c.id) {
    <div data-testid="compte-ligne">{{ c.iban }}</div>
  }
} @else {
  <p>Aucun compte</p>
}

@defer (on viewport) { <app-graphique /> } @placeholder { <p>Chargement…</p> }
```

## 5. *Zoneless* par défaut
Zone.js (qui détectait « magiquement » les changements) n'est plus utilisé par défaut. La détection de changements est pilotée par les signals : c'est plus rapide et plus prévisible. Conséquence : on met les données d'affichage dans des **signals**.

## 6. Appels HTTP
```ts
// Classique, toujours valable
comptes = toSignal(this.http.get<Account[]>('/api/accounts'), { initialValue: [] });

// API orientée signals (httpResource) : recharge automatiquement quand l'id change
compteId = input.required<string>();
compte = httpResource<Account>(() => `/api/accounts/${this.compteId()}`);
// dans le template : compte.value(), compte.isLoading(), compte.error()
```

## 7. Formulaires
- Les *Reactive Forms* (`FormGroup`, `FormControl`) existent toujours et sont désormais **typés**.
- **Signal Forms** est la nouvelle API, basée sur les signals. Elle est mise en avant en v22, à essayer dans la tâche S6-3.

## 8. Outillage
| Avant | Maintenant |
|---|---|
| Webpack | **esbuild + Vite** (builds beaucoup plus rapides) |
| Karma + Jasmine | **Vitest** par défaut pour les tests unitaires |
| Protractor (e2e) | abandonné → **Playwright** (ou Cypress) |
| TSLint | ESLint |

Commandes utiles : `ng new`, `ng generate component`, `ng serve --proxy-config proxy.conf.json`, `ng test`, `ng build`, `ng update`.

## 9. Côté testabilité (pour ta sœur)
Ajoute systématiquement des `data-testid="..."` sur les boutons, les champs et les messages importants. Avec de vrais `<button>` et `<label for>`, cela permet à Playwright d'utiliser `getByRole('button', { name: 'Valider le virement' })` : c'est robuste **et** accessible.
