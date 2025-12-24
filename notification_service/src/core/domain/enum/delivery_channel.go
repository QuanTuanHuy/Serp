/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type DeliveryChannel string

const (
	ChannelInApp DeliveryChannel = "IN_APP"
	ChannelEmail DeliveryChannel = "EMAIL"
	ChannelPush  DeliveryChannel = "PUSH"
	ChannelSMS   DeliveryChannel = "SMS"
)

func (dc DeliveryChannel) String() string {
	return string(dc)
}
