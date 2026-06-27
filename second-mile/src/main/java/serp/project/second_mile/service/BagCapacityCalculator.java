/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service;

import serp.project.second_mile.caller.dto.tms_order.TmsOrderOperationView;
import serp.project.second_mile.domain.Bag;
import serp.project.second_mile.dto.response.BagCapacitySettingsResponse;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static serp.project.second_mile.kernel.utils.CommonValueUtils.gramsToKilograms;
import static serp.project.second_mile.kernel.utils.CommonValueUtils.positiveOrDefault;
import static serp.project.second_mile.kernel.utils.CommonValueUtils.safeDouble;
import static serp.project.second_mile.kernel.utils.CommonValueUtils.safeInt;

public final class BagCapacityCalculator {
    private BagCapacityCalculator() {
    }

    public static boolean canFit(
            Bag bag,
            TmsOrderOperationView order,
            double extraWeight,
            double extraVolume,
            int extraOrders,
            BagCapacitySettingsResponse capacitySettings
    ) {
        double nextWeight = safeDouble(bag.getCurrentWeight()) + orderWeightKg(order) + extraWeight;
        double nextVolume = safeDouble(bag.getCurrentVolume()) + safeDouble(order.getTotalVolume()) + extraVolume;
        int nextOrders = safeInt(bag.getCurrentOrders()) + 1 + extraOrders;

        boolean withinWeight = nextWeight <= positiveOrDefault(bag.getMaxWeight(), capacitySettings.maxWeight());
        boolean withinVolume = nextVolume <= positiveOrDefault(bag.getMaxVolume(), capacitySettings.maxVolume());
        boolean withinOrders = nextOrders <= positiveOrDefault(bag.getMaxOrders(), capacitySettings.maxOrders());
        return withinWeight && withinVolume && withinOrders;
    }

    public static double remainingWeight(Bag bag, BagCapacitySettingsResponse capacitySettings) {
        return positiveOrDefault(bag.getMaxWeight(), capacitySettings.maxWeight()) - safeDouble(bag.getCurrentWeight());
    }

    public static double remainingVolume(Bag bag, BagCapacitySettingsResponse capacitySettings) {
        return positiveOrDefault(bag.getMaxVolume(), capacitySettings.maxVolume()) - safeDouble(bag.getCurrentVolume());
    }

    public static int remainingOrders(Bag bag, BagCapacitySettingsResponse capacitySettings) {
        return positiveOrDefault(bag.getMaxOrders(), capacitySettings.maxOrders()) - safeInt(bag.getCurrentOrders());
    }

    public static double orderWeightKg(TmsOrderOperationView order) {
        return gramsToKilograms(safeDouble(order.getTotalWeight()));
    }

    public static double normalizePositiveOrDefault(Double value, double fallback) {
        if (value == null) {
            return fallback;
        }
        if (value <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        return value;
    }

    public static int normalizePositiveOrDefault(Integer value, int fallback) {
        if (value == null) {
            return fallback;
        }
        if (value <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        return value;
    }

    public static List<AutoBagBin> planAutoBags(
            List<TmsOrderOperationView> orders,
            BagCapacitySettingsResponse capacitySettings
    ) {
        List<TmsOrderOperationView> sortedOrders = orders.stream()
                .sorted(Comparator.comparing((TmsOrderOperationView order) ->
                                sizeScore(order, capacitySettings.maxWeight(), capacitySettings.maxVolume()))
                        .reversed())
                .toList();

        List<MutableAutoBagBin> bins = new ArrayList<>();
        for (TmsOrderOperationView order : sortedOrders) {
            MutableAutoBagBin selected = null;
            for (MutableAutoBagBin bin : bins) {
                if (bin.canFit(order)) {
                    selected = bin;
                    break;
                }
            }
            if (selected == null) {
                selected = new MutableAutoBagBin(
                        capacitySettings.maxWeight(),
                        capacitySettings.maxVolume(),
                        capacitySettings.maxOrders()
                );
                if (!selected.canFit(order)) {
                    throw new AppException(ErrorCode.INVALID_REQUEST, "Order exceeds single bag capacity.");
                }
                bins.add(selected);
            }
            selected.add(order);
        }
        return bins.stream()
                .map(MutableAutoBagBin::toAutoBagBin)
                .toList();
    }

    private static double sizeScore(TmsOrderOperationView order, double maxWeight, double maxVolume) {
        double weightRatio = orderWeightKg(order) / maxWeight;
        double volumeRatio = safeDouble(order.getTotalVolume()) / maxVolume;
        return Math.max(weightRatio, volumeRatio);
    }

    public record AutoBagBin(
            List<TmsOrderOperationView> orders,
            double totalWeight,
            double totalVolume
    ) {
        public List<String> orderCodes() {
            return orders.stream()
                    .map(TmsOrderOperationView::getOrderCode)
                    .collect(Collectors.toList());
        }
    }

    private static class MutableAutoBagBin {
        private final double maxWeight;
        private final double maxVolume;
        private final int maxOrders;
        private final List<TmsOrderOperationView> orders = new ArrayList<>();
        private double totalWeight = 0.0;
        private double totalVolume = 0.0;

        private MutableAutoBagBin(double maxWeight, double maxVolume, int maxOrders) {
            this.maxWeight = maxWeight;
            this.maxVolume = maxVolume;
            this.maxOrders = maxOrders;
        }

        private boolean canFit(TmsOrderOperationView order) {
            return (totalWeight + orderWeightKg(order) <= maxWeight)
                    && (totalVolume + safeDouble(order.getTotalVolume()) <= maxVolume)
                    && (orders.size() + 1 <= maxOrders);
        }

        private void add(TmsOrderOperationView order) {
            orders.add(order);
            totalWeight += orderWeightKg(order);
            totalVolume += safeDouble(order.getTotalVolume());
        }

        private AutoBagBin toAutoBagBin() {
            return new AutoBagBin(List.copyOf(orders), totalWeight, totalVolume);
        }
    }
}
