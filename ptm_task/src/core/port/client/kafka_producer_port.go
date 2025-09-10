/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import "context"

type IKafkaProducerPort interface {
	SendMessage(ctx context.Context, topic string, key string, payload any) error
	SendMessageAsync(ctx context.Context, topic string, key string, payload any) error
}
