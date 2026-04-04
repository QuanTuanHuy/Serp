'use client';

import { useState } from 'react';
import { cn } from '@/shared/utils';
import {
  useGetTodosQuery,
  useCreateTodoMutation,
  useUpdateTodoMutation,
  useDeleteTodoMutation,
} from '../api/ttcrsApi';

export function TodoList() {
  const [input, setInput] = useState('');
  const { data: todos, isLoading, isError } = useGetTodosQuery();
  const [createTodo, { isLoading: isCreating }] = useCreateTodoMutation();
  const [updateTodo] = useUpdateTodoMutation();
  const [deleteTodo] = useDeleteTodoMutation();

  const handleAdd = async () => {
    const title = input.trim();
    if (!title) return;
    await createTodo({ title });
    setInput('');
  };

  const handleToggle = (id: number, completed: boolean) => {
    updateTodo({ id, completed: !completed });
  };

  const handleDelete = (id: number) => {
    deleteTodo(id);
  };

  const completedCount = todos?.filter((t) => t.completed).length ?? 0;
  const totalCount = todos?.length ?? 0;

  return (
    <div className="mx-auto max-w-lg space-y-6 p-6">
      <div>
        <h1 className="text-2xl font-bold">Todo List — ttcrs demo</h1>
        {totalCount > 0 && (
          <p className="text-sm text-muted-foreground mt-1">
            {completedCount}/{totalCount} hoàn thành
          </p>
        )}
      </div>

      {/* Add input */}
      <div className="flex gap-2">
        <input
          className="flex-1 rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
          placeholder="Nhập công việc mới..."
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleAdd()}
          disabled={isCreating}
        />
        <button
          onClick={handleAdd}
          disabled={isCreating || !input.trim()}
          className="rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/90 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {isCreating ? 'Đang thêm...' : 'Thêm'}
        </button>
      </div>

      {/* States */}
      {isLoading && (
        <p className="text-sm text-muted-foreground text-center py-4">Đang tải...</p>
      )}
      {isError && (
        <div className="rounded-md border border-destructive/50 bg-destructive/10 px-4 py-3 text-sm text-destructive">
          Không kết nối được backend. Hãy chắc ttcrs đang chạy ở port 8093.
        </div>
      )}

      {/* Todo list */}
      {!isLoading && !isError && (
        <ul className="space-y-2">
          {todos?.length === 0 && (
            <li className="text-center text-sm text-muted-foreground py-8">
              Chưa có todo nào. Thêm công việc đầu tiên!
            </li>
          )}
          {todos?.map((todo) => (
            <li
              key={todo.id}
              className="flex items-center gap-3 rounded-md border bg-card px-4 py-3 shadow-sm"
            >
              <input
                type="checkbox"
                checked={todo.completed}
                onChange={() => handleToggle(todo.id, todo.completed)}
                className="h-4 w-4 rounded border-input accent-primary cursor-pointer"
              />
              <span
                className={cn(
                  'flex-1 text-sm',
                  todo.completed && 'line-through text-muted-foreground'
                )}
              >
                {todo.title}
              </span>
              <button
                onClick={() => handleDelete(todo.id)}
                className="text-xs text-destructive hover:underline"
              >
                Xóa
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
