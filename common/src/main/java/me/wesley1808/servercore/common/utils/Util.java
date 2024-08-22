package me.wesley1808.servercore.common.utils;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Util {
    public static final AtomicInteger WORLD_COUNTER = new AtomicInteger();

    public static <T> boolean iteratePage(List<T> list, int page, int pageSize, BiConsumer<T, Integer> consumer) {
        int index = getIndex(page, pageSize);
        int toIndex = Math.min(index + pageSize, list.size());

        if (toIndex <= index) {
            return false;
        }

        for (T value : list.subList(index, toIndex)) {
            consumer.accept(value, ++index);
        }

        return true;
    }

    public static int getIndex(int page, int pageSize) {
        return pageSize * (page - 1);
    }

    public static int getPage(int index, int pageSize) {
        return (index + pageSize - 1) / pageSize;
    }

    public static <K, V extends Comparable<V>> List<Map.Entry<K, V>> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> entries = new ObjectArrayList<>(map.entrySet());
        entries.sort((c1, c2) -> c2.getValue().compareTo(c1.getValue()));
        return entries;
    }

    public static CompletableFuture<Suggestions> suggestAll(SuggestionsBuilder builder, String... suggestions) {
        for (String suggestion : suggestions) {
            builder.suggest(suggestion);
        }

        return builder.buildFuture();
    }

    public static boolean hasTasks(GoalSelector selector) {
        return hasTasks(selector, null);
    }

    public static boolean hasTasks(GoalSelector selector, @Nullable Predicate<Goal> predicate) {
        for (WrappedGoal wrapped : selector.getAvailableGoals()) {
            if (wrapped.isRunning() && (predicate == null || predicate.test(wrapped.getGoal()))) {
                return true;
            }
        }
        return false;
    }

    public static <T, R> ObjectArrayList<R> map(Collection<T> collection, Function<T, R> function) {
        ObjectArrayList<R> result = new ObjectArrayList<>(collection.size());
        for (T value : collection) {
            R mapped = function.apply(value);
            if (mapped != null) {
                result.add(mapped);
            }
        }

        return result;
    }
}
