export interface TodoItem {
  id: number;
  title: string;
  completed: boolean;
  createdAt: number;
}

export interface CreateTodoRequest {
  title: string;
}

export interface UpdateTodoRequest {
  title?: string;
  completed?: boolean;
}
