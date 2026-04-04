import { api } from '@/lib/store/api';
import type { TodoItem, CreateTodoRequest, UpdateTodoRequest } from '../types';

export const ttcrsApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getTodos: builder.query<TodoItem[], void>({
      query: () => ({ url: '/todos', method: 'GET' }),
      extraOptions: { service: 'ttcrs' },
      providesTags: (result) =>
        result
          ? [
              ...result.map(({ id }) => ({ type: 'Todo' as const, id })),
              { type: 'Todo', id: 'LIST' },
            ]
          : [{ type: 'Todo', id: 'LIST' }],
    }),

    createTodo: builder.mutation<TodoItem, CreateTodoRequest>({
      query: (body) => ({ url: '/todos', method: 'POST', body }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: [{ type: 'Todo', id: 'LIST' }],
    }),

    updateTodo: builder.mutation<TodoItem, { id: number } & UpdateTodoRequest>({
      query: ({ id, ...body }) => ({ url: `/todos/${id}`, method: 'PUT', body }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: (_result, _error, { id }) => [{ type: 'Todo', id }],
    }),

    deleteTodo: builder.mutation<void, number>({
      query: (id) => ({ url: `/todos/${id}`, method: 'DELETE' }),
      extraOptions: { service: 'ttcrs' },
      invalidatesTags: (_result, _error, id) => [
        { type: 'Todo', id },
        { type: 'Todo', id: 'LIST' },
      ],
    }),
  }),
});

export const {
  useGetTodosQuery,
  useCreateTodoMutation,
  useUpdateTodoMutation,
  useDeleteTodoMutation,
} = ttcrsApi;
