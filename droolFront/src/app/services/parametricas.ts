import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { catchError, tap, timeout, map } from 'rxjs/operators';

// Importar interfaces dinámicas desde el modelo
import {
  EntidadParametrica,
  ParametricasVigentesDTO,
  EvaluacionParametricasDTO,
  ResultadoEvaluacionDTO,
  CatalogoParametricasDTO,
  FiltroParametricas
} from '../models/parametricas';

@Injectable({
  providedIn: 'root'
})
export class Parametricas {
  // Configuración base (igual que drools.service.ts)
  private baseUrl = 'http://172.28.138.56:8080/api';
  private httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json'
    })
  };

  // Cache para catálogo dinámico
  private catalogoSubject = new BehaviorSubject<CatalogoParametricasDTO | null>(null);

  constructor(private http: HttpClient) {}

  // =============================================================
  // MÉTODOS PRINCIPALES - FUNCIONALIDAD CORE
  // =============================================================

  /**
   * Obtener paramétricas vigentes para una fecha específica
   */
  getParametricasVigentes(fecha: string): Observable<ParametricasVigentesDTO | string> {
    console.log('Fetching parametricas vigentes for date:', fecha);
    return this.http.get<ParametricasVigentesDTO>(`${this.baseUrl}/parametricas/vigentes?fecha=${fecha}`).pipe(
      tap(result => console.log('Parametricas vigentes received:', result)),
      catchError(error => {
        console.error('Error fetching parametricas vigentes:', error);
        return of('Backend no disponible');
      })
    );
  }

  /**
   * Evaluar requerimiento con paramétricas temporales
   */
  evaluarParametricas(evaluacion: EvaluacionParametricasDTO): Observable<ResultadoEvaluacionDTO | string> {
    console.log('Evaluating parametricas:', evaluacion);
    return this.http.post<ResultadoEvaluacionDTO>(`${this.baseUrl}/parametricas/evaluar`, evaluacion, this.httpOptions).pipe(
      timeout(10000),
      tap(result => console.log('Evaluation result received:', result)),
      catchError(error => {
        console.error('Error evaluating parametricas:', error);
        return of('Backend no disponible');
      })
    );
  }

  /**
   * Obtener catálogo dinámico de paramétricas
   */
  getCatalogoCompleto(): Observable<CatalogoParametricasDTO | string> {
    console.log('Fetching catalogo completo...');
    return this.http.get<CatalogoParametricasDTO>(`${this.baseUrl}/parametricas/catalogo-completo`).pipe(
      tap(catalogo => {
        console.log('Catalogo received:', catalogo);
        this.catalogoSubject.next(catalogo);
      }),
      catchError(error => {
        console.error('Error fetching catalogo:', error);
        this.catalogoSubject.next(null);
        return of('Backend no disponible');
      })
    );
  }

  /**
   * Health check del sistema de paramétricas
   */
  healthCheck(): Observable<string | null> {
    return this.http.get<string>(`${this.baseUrl}/parametricas/health`).pipe(
      catchError(error => {
        console.error('Health check failed:', error);
        return of('Backend no disponible');
      })
    );
  }

  // =============================================================
  // CRUD DINÁMICO - FUNCIONA CON CUALQUIER ENTIDAD
  // =============================================================

  /**
   * Método genérico para obtener entidades de cualquier tipo
   */
  getEntidades(nombreEntidad: string, filtros?: FiltroParametricas): Observable<EntidadParametrica[] | string> {
    console.log(`Fetching entidades ${nombreEntidad}...`, filtros);

    let url = `${this.baseUrl}/${nombreEntidad}`;
    if (filtros?.anio) {
      url += `?anio=${filtros.anio}`;
    }

    return this.http.get<EntidadParametrica[]>(url).pipe(
      catchError(error => {
        console.error(`Error fetching ${nombreEntidad}:`, error);
        return of('Backend no disponible');
      })
    );
  }

  /**
   * Método genérico para crear entidades
   */
  createEntidad(nombreEntidad: string, entidad: Omit<EntidadParametrica, 'id'>): Observable<EntidadParametrica | string> {
    console.log(`Creating ${nombreEntidad}...`, entidad);
    return this.http.post<EntidadParametrica>(`${this.baseUrl}/${nombreEntidad}`, entidad, this.httpOptions).pipe(
      catchError(error => {
        console.error(`Error creating ${nombreEntidad}:`, error);
        return of('Backend no disponible');
      })
    );
  }

  /**
   * Método genérico para actualizar entidades
   */
  updateEntidad(nombreEntidad: string, id: number, entidad: Partial<EntidadParametrica>): Observable<EntidadParametrica | string> {
    console.log(`Updating ${nombreEntidad}...`, id, entidad);
    return this.http.put<EntidadParametrica>(`${this.baseUrl}/${nombreEntidad}/${id}`, entidad, this.httpOptions).pipe(
      catchError(error => {
        console.error(`Error updating ${nombreEntidad}:`, error);
        return of('Backend no disponible');
      })
    );
  }

  /**
   * Método genérico para eliminar entidades
   */
  deleteEntidad(nombreEntidad: string, id: number): Observable<boolean> {
    console.log(`Deleting ${nombreEntidad}...`, id);
    return this.http.delete<void>(`${this.baseUrl}/${nombreEntidad}/${id}`).pipe(
      map(() => true),
      catchError(error => {
        console.error(`Error deleting ${nombreEntidad}:`, error);
        return of(false);
      })
    );
  }

  /**
   * Método genérico para obtener una entidad específica
   */
  getEntidad(nombreEntidad: string, id: number): Observable<EntidadParametrica | string> {
    console.log(`Fetching ${nombreEntidad} with id:`, id);
    return this.http.get<EntidadParametrica>(`${this.baseUrl}/${nombreEntidad}/${id}`).pipe(
      catchError(error => {
        console.error(`Error fetching ${nombreEntidad} ${id}:`, error);
        return of('Backend no disponible');
      })
    );
  }

  // =============================================================
  // MÉTODOS DE CONVENIENCIA - WRAPPERS PARA ENTIDADES ESPECÍFICAS
  // =============================================================



  getUitVigente(fecha: string): Observable<EntidadParametrica | string> {
    console.log('Fetching UIT vigente for date:', fecha);
    return this.http.get<EntidadParametrica>(`${this.baseUrl}/uit/vigente?fecha=${fecha}`).pipe(
      catchError(error => {
        console.warn('Error fetching UIT vigente:', error);
        return of('Backend no disponible');
      })
    );
  }



  // =============================================================
  // GETTER PARA CATÁLOGO (observable para componentes)
  // =============================================================

  get catalogo$(): Observable<CatalogoParametricasDTO | null> {
    return this.catalogoSubject.asObservable();
  }


}
