import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class Notification {

  constructor(private snackBar: MatSnackBar) { }

  success(message: string, duration: number = 3000) {
    this.snackBar.open(message, 'Cerrar', {
      duration,
      panelClass: ['success-snackbar'],
      horizontalPosition: 'right',
      verticalPosition: 'top'
    });
  }

  error(message: string, duration: number = 5000) {
    this.snackBar.open(message, 'Cerrar', {
      duration,
      panelClass: ['error-snackbar'],
      horizontalPosition: 'right',
      verticalPosition: 'top'
    });
  }

  warning(message: string, duration: number = 4000) {
    this.snackBar.open(message, 'Cerrar', {
      duration,
      panelClass: ['warning-snackbar'],
      horizontalPosition: 'right',
      verticalPosition: 'top'
    });
  }

  info(message: string, duration: number = 3000) {
    this.snackBar.open(message, 'Cerrar', {
      duration,
      panelClass: ['info-snackbar'],
      horizontalPosition: 'right',
      verticalPosition: 'top'
    });
  }

  // ✅ Métodos adicionales que faltan (alias para consistencia)
  showSuccess(message: string, duration: number = 3000) {
    this.success(message, duration);
  }

  showError(message: string, duration: number = 5000) {
    this.error(message, duration);
  }

  showWarning(message: string, duration: number = 4000) {
    this.warning(message, duration);
  }

  showInfo(message: string, duration: number = 3000) {
    this.info(message, duration);
  }

  // ✅ Métodos adicionales útiles
  showMessage(message: string, action: string = 'Cerrar', duration: number = 3000) {
    this.snackBar.open(message, action, {
      duration,
      horizontalPosition: 'right',
      verticalPosition: 'top'
    });
  }

  // Para mostrar notificaciones sin botón de cerrar
  showSimple(message: string, duration: number = 2000) {
    this.snackBar.open(message, '', {
      duration,
      horizontalPosition: 'center',
      verticalPosition: 'bottom'
    });
  }

  // Para mostrar notificaciones persistentes (que no se cierran automáticamente)
  showPersistent(message: string, type: 'success' | 'error' | 'warning' | 'info' = 'info') {
    this.snackBar.open(message, 'Cerrar', {
      panelClass: [`${type}-snackbar`],
      horizontalPosition: 'right',
      verticalPosition: 'top'
      // Sin duration = no se cierra automáticamente
    });
  }

  // Para cerrar todas las notificaciones
  dismissAll() {
    this.snackBar.dismiss();
  }
}
