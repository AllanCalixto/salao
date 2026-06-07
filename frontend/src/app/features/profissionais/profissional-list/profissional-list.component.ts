import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ProfissionalService } from '../../../core/services/profissional.service';
import { ProfissionalResponse, ESPECIALIDADE_LABELS, Especialidade } from '../../../core/models';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-profissional-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatTableModule,
    MatIconModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatDialogModule,
    MatSnackBarModule,
    LoadingSpinnerComponent,
  ],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>Profissionais</h1>
        <button mat-raised-button color="primary" routerLink="/profissionais/novo">
          <mat-icon>add</mat-icon>
          Novo Profissional
        </button>
      </div>

      @if (loading()) {
        <app-loading-spinner />
      } @else if (profissionais().length === 0) {
        <div class="empty-state">
          <mat-icon>badge</mat-icon>
          <p>Nenhum profissional encontrado</p>
          <button mat-stroked-button color="primary" routerLink="/profissionais/novo">
            Cadastrar primeiro profissional
          </button>
        </div>
      } @else {
        <mat-table [dataSource]="profissionais()" class="mat-elevation-z2">
          <ng-container matColumnDef="id">
            <mat-header-cell *matHeaderCellDef>ID</mat-header-cell>
            <mat-cell *matCellDef="let prof">{{ prof.id }}</mat-cell>
          </ng-container>

          <ng-container matColumnDef="nome">
            <mat-header-cell *matHeaderCellDef>Nome</mat-header-cell>
            <mat-cell *matCellDef="let prof">{{ prof.nome }}</mat-cell>
          </ng-container>

          <ng-container matColumnDef="especialidade">
            <mat-header-cell *matHeaderCellDef>Especialidade</mat-header-cell>
            <mat-cell *matCellDef="let prof">
              <mat-chip>{{ getEspecialidadeLabel(prof.especialidade) }}</mat-chip>
            </mat-cell>
          </ng-container>

          <ng-container matColumnDef="servicos">
            <mat-header-cell *matHeaderCellDef>Serviços</mat-header-cell>
            <mat-cell *matCellDef="let prof">
              <div class="servicos-list">
                @for (servico of prof.servicos; track servico) {
                  <mat-chip>{{ servico }}</mat-chip>
                }
              </div>
            </mat-cell>
          </ng-container>

          <ng-container matColumnDef="acoes">
            <mat-header-cell *matHeaderCellDef>Ações</mat-header-cell>
            <mat-cell *matCellDef="let prof">
              <button mat-icon-button color="primary" [routerLink]="['/profissionais', prof.id]">
                <mat-icon>edit</mat-icon>
              </button>
              <button mat-icon-button color="warn" (click)="confirmDelete(prof)">
                <mat-icon>delete</mat-icon>
              </button>
            </mat-cell>
          </ng-container>

          <mat-header-row *matHeaderRowDef="displayedColumns"></mat-header-row>
          <mat-row *matRowDef="let row; columns: displayedColumns;"></mat-row>
        </mat-table>
      }
    </div>
  `,
  styles: [
    `
      .servicos-list {
        display: flex;
        gap: 4px;
        flex-wrap: wrap;
      }

      .mat-column-acoes {
        width: 120px;
        text-align: center;
      }

      .mat-column-especialidade {
        width: 150px;
      }

      .mat-column-servicos {
        min-width: 200px;
      }
    `,
  ],
})
export class ProfissionalListComponent implements OnInit {
  private profissionalService = inject(ProfissionalService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  readonly ESPECIALIDADE_LABELS = ESPECIALIDADE_LABELS;
  loading = signal(true);
  profissionais = signal<ProfissionalResponse[]>([]);
  displayedColumns = ['id', 'nome', 'especialidade', 'servicos', 'acoes'];

  ngOnInit(): void {
    this.loadProfissionais();
  }

  getEspecialidadeLabel(especialidade: Especialidade): string {
    return ESPECIALIDADE_LABELS[especialidade];
  }

  loadProfissionais(): void {
    this.loading.set(true);
    this.profissionalService.listarTodos().subscribe({
      next: (data) => {
        this.profissionais.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.snackBar.open('Erro ao carregar profissionais', 'Fechar', { duration: 3000 });
      },
    });
  }

  confirmDelete(prof: ProfissionalResponse): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Confirmar exclusão',
        message: `Deseja realmente excluir o profissional "${prof.nome}"?`,
        confirmText: 'Excluir',
        cancelText: 'Cancelar',
      } as ConfirmDialogData,
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.profissionalService.deletar(prof.id).subscribe({
          next: () => {
            this.snackBar.open('Profissional excluído com sucesso', 'Fechar', { duration: 3000 });
            this.loadProfissionais();
          },
          error: () => {
            this.snackBar.open('Erro ao excluir profissional', 'Fechar', { duration: 3000 });
          },
        });
      }
    });
  }
}