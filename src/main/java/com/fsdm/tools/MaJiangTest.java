package com.fsdm.tools;

import com.google.common.collect.Lists;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by @author liushouyun on 2024/9/20 3:58 下午.
 * <p>
 * 牌型： 万，饼，条。 1-9。 每样四张
 * 胡牌： n顺子+n刻子+1将
 */
public class MaJiangTest {
    static int max = 14;
    static String[] titleList = {"万", "饼", "条"};

    public static void main(String[] args) {


        // 拿到所有牌型
        List<Pai> all = getAll();
        System.out.println("getAll");
        // 拿到所有牌
        all = buildAll(all);
        System.out.println("buildAll");
        // 计算出所有可能14张牌的组合
        final List<List<Pai>> cal = cal(all);
        System.out.println("cal");
        // 将组合结果判定是否胡牌
        final List<List<Pai>> huList = cal.stream().filter(MaJiangTest::isHu).collect(Collectors.toList());
        System.out.println("isHu");
        System.out.println(huList);
    }

    private static List<Pai> getAll() {
        return IntStream.range(1, 10).boxed()
                .flatMap(i -> Arrays.stream(titleList).map(t -> (new Pai(t, i))))
                .collect(Collectors.toList());
    }

    private static List<Pai> buildAll(List<Pai> all) {
        // 牌型*4
        return all.stream()
                .flatMap(p -> Lists.newArrayList(p, p, p, p).stream())
                .collect(Collectors.toList());
    }

    private static List<List<Pai>> cal(List<Pai> all) {
        List<List<Pai>> pais = new ArrayList<>();
        doCal(all, 0, new ArrayList<>(), pais);
        return pais;
    }

    private static void doCal(List<Pai> all, int start, List<Pai> curr, List<List<Pai>> pais) {
        if (curr.size() == max) {
            pais.add(new ArrayList<>(curr));
            return;
        }
        for (int i = start; i < all.size(); i++) {
            curr.add(all.get(i));
            doCal(all, i + 1, curr, pais);
            curr.remove(curr.size() - 1);
        }
    }


    private static boolean isHu(List<Pai> list) {
        // group&sort
        final Map<String, List<Pai>> map = list.stream()
                .collect(Collectors.groupingBy(Pai::getTitle))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .sorted(Comparator.comparingInt(Pai::getNumber))
                                .collect(Collectors.toList()))
                );
        boolean isHu = true;
        for (Map.Entry<String, List<Pai>> entry : map.entrySet()) {
            final List<Pai> value = entry.getValue();
            int jiangCount = 0;
            for (int i = 0; i < value.size(); ) {
                if (i + 2 == value.size() - 1) {
                    if (jiangCount < 1 && isJiang(value.subList(i, 2))) {
                        break;
                    }
                }
                // TODO: 2024/9/20 这里先忽略刻子和顺子出现重叠产生误判的情况
                // 如果 刻子/顺子 与将牌产生重叠以刻子/顺子为主
                final List<Pai> pais = value.subList(i, 3);
                if (isKe(pais)) {
                    i += 3;
                } else if (isShun(pais)) {
                    i += 3;
                } else if (jiangCount < 1 && isJiang(value.subList(i, 2))) {
                    i += 2;
                    jiangCount++;
                } else {
                    isHu = false;
                    break;
                }
            }
            if (!isHu) {
                break;
            }
        }
        return isHu;
    }

    private static boolean isKe(List<Pai> pais) {
        return pais.get(0).getNumber() == pais.get(1).getNumber() && pais.get(0).getNumber() == pais.get(2).getNumber();
    }

    private static boolean isShun(List<Pai> pais) {
        return pais.get(0).getNumber() + 1 == pais.get(1).getNumber() && pais.get(0).getNumber() + 2 == pais.get(2).getNumber();
    }

    private static boolean isJiang(List<Pai> pais) {
        return pais.get(0).getNumber() == pais.get(1).getNumber();
    }


    private static class Pai {
        private String title;
        private Integer number;

        public Pai(String title, Integer number) {
            this.title = title;
            this.number = number;
        }

        @Override
        public String toString() {
            return number + title;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(Integer number) {
            this.number = number;
        }
    }
}