import java.lang.reflect.Array;
import java.util.*;

class RecursPacking {

    static long Height;
    public static long endSortingTime1;
    public static long endSortingTime2;

    Types.Result spprg(long WidthStrip, ArrayList<Types.CoupleWH> rectanglesWH, String sorting) {

        int wh = 0;
        int section = 0;
        int intermediateW;

        long bx1;
        long bx2;
        long by;

        Map<Integer, Types.Rectangle> rectangles = new HashMap<>();
        ArrayList<Types.Rectangles> sections = new ArrayList<>();
        ArrayList<Types.Rectangle> newRectangles = new ArrayList<>();
        ArrayList<Types.CoupleWH> remaining = new ArrayList<>();
        ArrayList<Integer> sorted_indexes = new ArrayList<>();
        ArrayList<Types.Areas> areas = new ArrayList<>();
        ArrayList<Types.Area> newAreas = new ArrayList<>();
        ArrayList<Types.Area> verticalAreas = new ArrayList<>();
        ArrayList<Types.Barrier> barriers = new ArrayList<>();

        if (sorting.equals("width"))
            wh = 0;
        else if (sorting.equals("height"))
            wh = 1;

        for (int i = 0; i < rectanglesWH.size(); i++) {
            rectangles.put(i, null);
        }

        for (Types.CoupleWH element : rectanglesWH) {
            remaining.add(new Types.CoupleWH(element.getW(), element.getH()));
        }

        for (Types.CoupleWH element : remaining) {
            if (element.getW() > element.getH()) {
                intermediateW = element.getW();
                element.setW(element.getH());
                element.setH(intermediateW);
            }
        }

        ArrayList<Types.CoupleWH> copy = new ArrayList<>(remaining);

        if (wh == 0) {
            Comparator<Types.CoupleWH> c = Collections.reverseOrder(new Types.SortByWidth());
            endSortingTime1 = System.currentTimeMillis();
            copy.sort(c);
            endSortingTime2 = System.currentTimeMillis();
        }

        //TODO для невозрастающей высоты
        for (Types.CoupleWH element : copy) {
            sorted_indexes.add(remaining.indexOf(element));
        }

        long ySection;
        long HeightSection;
        long x, y, w, h, H;
        x = 0;
        y = 0;
        H = 0;

        while (!sorted_indexes.isEmpty()) {

            int idx;
            idx = sorted_indexes.get(0);
            sorted_indexes.remove(0);
            Types.CoupleWH r = remaining.get(idx);

            if (r.getH() > WidthStrip) {
                rectangles.put(idx, new Types.Rectangle(x, y, r.getW(), r.getH()));
                newRectangles.add(new Types.Rectangle(x, y, r.getW(), r.getH()));
                x = r.getW();
                y = H;
                w = WidthStrip - r.getW();
                h = r.getH();
                H = H + r.getH();
                ySection = y;
                HeightSection = r.getH();
            } else {
                rectangles.put(idx, new Types.Rectangle(x, y, r.getH(), r.getW()));
                newRectangles.add(new Types.Rectangle(x, y, r.getH(), r.getW()));
                x = r.getH();
                y = H;
                w = WidthStrip - r.getH();
                h = r.getW();
                H = H + r.getW();
                ySection = y;
                HeightSection = r.getW();
            }

            newAreas.add(new Types.Area(x, y, w, h));

            //инициализация начальных барьеров;
            barriers.add(new Types.Barrier(newRectangles.get(0).getX(), newRectangles.get(0).getX() + newRectangles.get(0).getW(), newRectangles.get(0).getY() + newRectangles.get(0).getH()));

            if (y == 0) {
                barriers.add(new Types.Barrier(x, x + w, y));
            } else if (newRectangles.get(0).getW() < sections.get(section - 1).getRectangles().get(0).getW()) {
                barriers.add(new Types.Barrier(
                        sections.get(section - 1).getRectangles().get(0).getX() + newRectangles.get(0).getW(),
                        sections.get(section - 1).getBarriers().get(0).getX2(),
                        sections.get(section - 1).getBarriers().get(0).getY()
                ));
            } else {
                barriers.add(new Types.Barrier(
                        sections.get(section - 1).getBarriers().get(0).getX1(),
                        sections.get(section - 1).getBarriers().get(0).getX2(),
                        sections.get(section - 1).getBarriers().get(0).getY()
                ));
            }


            //TODO рассчет барьеров
            if (section == 0)
                recursive_packing(x, y, w, h, 1, ySection, HeightSection, remaining, sorted_indexes, rectangles, newAreas, barriers, newRectangles, null);
            else
                recursive_packing(x, y, w, h, 1, ySection, HeightSection, remaining, sorted_indexes, rectangles, newAreas, barriers, newRectangles, sections.get(section - 1).getBarriers());

            //TODO формирование вертикальных областей
            if (newAreas.size() == 1 &&
                    newAreas.get(0).getX() == x &&
                    newAreas.get(0).getY() == y &&
                    newAreas.get(0).getW() == w &&
                    newAreas.get(0).getH() == h &&
                    section != 0) {

                for (Types.Area verticalArea : verticalAreas) {
                    if (newAreas.get(0).getX() <= verticalArea.getX() &&
                            (newAreas.get(0).getX() + newAreas.get(0).getW()) >= (verticalArea.getX() + verticalArea.getW()) &&
                            newAreas.get(0).getY() == (verticalArea.getY() + verticalArea.getH())) {

                        verticalArea.setX(verticalArea.getX());
                        verticalArea.setW(verticalArea.getW());
                        verticalArea.setH(verticalArea.getH() + newAreas.get(0).getH());
                    }

                    if (newAreas.get(0).getX() >= verticalArea.getX() &&
                            (newAreas.get(0).getX() + newAreas.get(0).getW()) <= (verticalArea.getX() + verticalArea.getW()) &&
                            newAreas.get(0).getY() == (verticalArea.getY() + verticalArea.getH())) {

                        verticalArea.setX(newAreas.get(0).getX());
                        verticalArea.setW(newAreas.get(0).getW());
                        verticalArea.setH(verticalArea.getH() + newAreas.get(0).getH());
                    }
                }

                for (int i = 0; i < areas.get(section - 1).getAreas().size(); i++) {
                    if (newAreas.get(0).getX() <= areas.get(section - 1).getAreas().get(i).getX() &&
                            (newAreas.get(0).getX() + newAreas.get(0).getW()) >= (areas.get(section - 1).getAreas().get(i).getX() + areas.get(section - 1).getAreas().get(i).getW()) &&
                            newAreas.get(0).getY() == (areas.get(section - 1).getAreas().get(i).getY() + areas.get(section - 1).getAreas().get(i).getH())) {

                        Types.Area newVerticalArea = new Types.Area(
                                areas.get(section - 1).getAreas().get(i).getX(),
                                areas.get(section - 1).getAreas().get(i).getY(),
                                areas.get(section - 1).getAreas().get(i).getW(),
                                newAreas.get(0).getH() + areas.get(section - 1).getAreas().get(i).getH());

                        if (!verticalAreas.contains(newVerticalArea)) verticalAreas.add(newVerticalArea);
                    }

                    if (newAreas.get(0).getX() >= areas.get(section - 1).getAreas().get(i).getX() &&
                            (newAreas.get(0).getX() + newAreas.get(0).getW()) <= (areas.get(section - 1).getAreas().get(i).getX() + areas.get(section - 1).getAreas().get(i).getW()) &&
                            newAreas.get(0).getY() == (areas.get(section - 1).getAreas().get(i).getY() + areas.get(section - 1).getAreas().get(i).getH())) {

                        Types.Area newVerticalArea = new Types.Area(
                                newAreas.get(0).getX(),
                                areas.get(section - 1).getAreas().get(i).getY(),
                                newAreas.get(0).getW(),
                                newAreas.get(0).getH() + areas.get(section - 1).getAreas().get(i).getH());

                        if (!verticalAreas.contains(newVerticalArea)) verticalAreas.add(newVerticalArea);
                    }
                }
            }

            if (newAreas.size() > 1) newAreas = new PackingHelper().mergingAreas(newAreas);

            areas.add(new Types.Areas(section, new ArrayList<>(newAreas)));
            sections.add(new Types.Rectangles(section, new ArrayList<>(newRectangles), new ArrayList<>(barriers)));

            System.out.println(barriers);

            newAreas.clear();
            newRectangles.clear();
            barriers.clear();

            x = 0;
            y = H;
            section++;
            Height = H;
        }

//        ArrayList<Types.Area> mon = new ArrayList<>(verticalAreas);
//        Comparator<Types.Area> c = Collections.reverseOrder(new Types.sortAreasByHeight());
//        mon.sort(c);
//        System.out.println(mon);
//        System.out.println(mon.get(0).getX() + ", " + mon.get(0).getY() + ", " + mon.get(0).getW() + ", " + mon.get(0).getH());
//        System.out.println(mon.size());
//        for (Types.Rectangles s : sections) {
//            System.out.println("Секция " + s.getSection() + ": " + s.getRectangles());
//        }
        return new Types.Result(y, rectangles, areas);
    }

    private void recursive_packing(long x, long y, long w, long h, int wh, long ySection, long HeightSection,
                                   ArrayList<Types.CoupleWH> remaining, ArrayList<Integer> indexes, Map<Integer, Types.Rectangle> result,
                                   ArrayList<Types.Area> newAreas, ArrayList<Types.Barrier> barriers, ArrayList<Types.Rectangle> newRectangles, ArrayList<Types.Barrier> prevBarriers) {

        int lastBarrier = barriers.size() - 1;
        int lastArea = newAreas.size() - 1;
        int lastRectangle = newRectangles.size() - 1;

        int priority = 6;
        int orientation = 0;
        int best = 0;
        int psi, d;
        long min_w, min_h;

        for (int id : indexes) {
            for (int j = 0; j < wh + 1; j++) {
                if ((priority > 1) &&
                        (remaining.get(id).getWidthOrHeight(j % 2) == w) &&
                        (remaining.get(id).getWidthOrHeight((1 + j) % 2) == h)) {
                    priority = 1;
                    orientation = j;
                    best = id;
                    break;
                } else if ((priority > 2) &&
                        (remaining.get(id).getWidthOrHeight(j % 2) == w) &&
                        (remaining.get(id).getWidthOrHeight((1 + j) % 2) < h)) {
                    priority = 2;
                    orientation = j;
                    best = id;
                } else if ((priority > 3) &&
                        (remaining.get(id).getWidthOrHeight(j % 2) < w) &&
                        (remaining.get(id).getWidthOrHeight((1 + j) % 2) == h)) {
                    priority = 3;
                    orientation = j;
                    best = id;
                } else if ((priority > 4) &&
                        (remaining.get(id).getWidthOrHeight(j % 2) < w) &&
                        (remaining.get(id).getWidthOrHeight((1 + j) % 2) < h)) {
                    priority = 4;
                    orientation = j;
                    best = id;
                } else if (priority > 5) {
                    priority = 5;
                    orientation = j;
                    best = id;
                }
            }
        }

        if (priority < 5) {

            newAreas.remove(lastArea);
            barriers.remove(lastBarrier);

            if (orientation == 0) {
                psi = remaining.get(best).getW();
                d = remaining.get(best).getH();
            } else {
                psi = remaining.get(best).getH();
                d = remaining.get(best).getW();
            }

            result.put(best, new Types.Rectangle(x, y, psi, d));
            newRectangles.add(new Types.Rectangle(x, y, psi, d));
            indexes.remove(new Integer(best));

            lastRectangle = newRectangles.size() - 1;

            if ((ySection + HeightSection) == (y + d)) barriers.add(new Types.Barrier(x, x + psi, y + d));
            lastBarrier = barriers.size() - 1;
            if (barriers.size() != 1 &&
                    barriers.get(0).getX2() == barriers.get(lastBarrier).getX1() &&
                    barriers.get(0).getY() == barriers.get(lastBarrier).getY()) {
                barriers.get(0).setX2(barriers.get(lastBarrier).getX2());
                barriers.remove(lastBarrier);
            }

            if (priority == 2) {
                newAreas.add(new Types.Area(x, y + d, w, h - d));
                barriers.add(new Types.Barrier(x, x + w, y + d));
                lastBarrier = barriers.size() - 1;
                barriers = barriersCheck(barriers, prevBarriers, lastBarrier, ySection, newRectangles.get(lastRectangle));
                recursive_packing(x, y + d, w, h - d, wh, ySection, HeightSection, remaining, indexes, result, newAreas, barriers, newRectangles, prevBarriers);
            } else if (priority == 3) {
                newAreas.add(new Types.Area(x + psi, y, w - psi, h));
                barriers.add(new Types.Barrier(x + psi, (x + psi) + (w - psi), y));
                lastBarrier = barriers.size() - 1;
                barriers = barriersCheck(barriers, prevBarriers, lastBarrier, ySection, newRectangles.get(lastRectangle));
                recursive_packing(x + psi, y, w - psi, h, wh, ySection, HeightSection, remaining, indexes, result, newAreas, barriers, newRectangles, prevBarriers);
            } else if (priority == 4) {

                min_w = Long.MAX_VALUE;
                min_h = Long.MAX_VALUE;

                for (Integer id : indexes) {
                    min_w = Math.min(min_w, remaining.get(id).getW());
                    min_h = Math.min(min_h, remaining.get(id).getH());
                }

                min_w = Math.min(min_h, min_w);
                min_h = min_w;

                if ((w - psi) < min_w) {
                    newAreas.add(new Types.Area(x + psi, y, w - psi, d));
                    barriers.add(new Types.Barrier(x + psi, (x + psi) + (w - psi), y));
                    lastBarrier = barriers.size() - 1;
                    barriers = barriersCheck(barriers, prevBarriers, lastBarrier, ySection, newRectangles.get(lastRectangle));
                    newAreas.add(new Types.Area(x, y + d, w, h - d));
                    barriers.add(new Types.Barrier(x, x + psi, y + d));
                    lastBarrier = barriers.size() - 1;
                    barriers = barriersCheck(barriers, prevBarriers, lastBarrier, ySection, newRectangles.get(lastRectangle));
                    recursive_packing(x, y + d, w, h - d, wh, ySection, HeightSection, remaining, indexes, result, newAreas, barriers, newRectangles, prevBarriers);
                } else if ((h - d) < min_h) {
                    newAreas.add(new Types.Area(x, y + d, psi, h - d));
                    barriers.add(new Types.Barrier(x, x + psi, y + d));
                    lastBarrier = barriers.size() - 1;
                    barriers = barriersCheck(barriers, prevBarriers, lastBarrier, ySection, newRectangles.get(lastRectangle));
                    newAreas.add(new Types.Area(x + psi, y, w - psi, h));
                    barriers.add(new Types.Barrier(x + psi, (x + psi) + (w - psi), y));
                    lastBarrier = barriers.size() - 1;
                    barriers = barriersCheck(barriers, prevBarriers, lastBarrier, ySection, newRectangles.get(lastRectangle));
                    recursive_packing(x + psi, y, w - psi, h, wh, ySection, HeightSection, remaining, indexes, result, newAreas, barriers, newRectangles, prevBarriers);
                } else if (psi < min_w) {
                    newAreas.add(new Types.Area(x + psi, y, w - psi, d));
                    barriers.add(new Types.Barrier(x + psi, (x + psi) + (w - psi), y));
                    lastBarrier = barriers.size() - 1;
                    barriers = barriersCheck(barriers, prevBarriers, lastBarrier, ySection, newRectangles.get(lastRectangle));
                    recursive_packing(x + psi, y, w - psi, d, wh, ySection, HeightSection, remaining, indexes, result, newAreas, barriers, newRectangles, prevBarriers);
                    newAreas.add(new Types.Area(x, y + d, w, h - d));
                    barriers.add(new Types.Barrier(x, x + psi, y + d));
                    lastBarrier = barriers.size() - 1;
                    barriers = barriersCheck(barriers, prevBarriers, lastBarrier, ySection, newRectangles.get(lastRectangle));
                    recursive_packing(x, y + d, w, h - d, wh, ySection, HeightSection, remaining, indexes, result, newAreas, barriers, newRectangles, prevBarriers);
                } else {
                    newAreas.add(new Types.Area(x, y + d, psi, h - d));
                    barriers.add(new Types.Barrier(x, x + psi, y + d));
                    lastBarrier = barriers.size() - 1;
                    barriers = barriersCheck(barriers, prevBarriers, lastBarrier, ySection, newRectangles.get(lastRectangle));
                    recursive_packing(x, y + d, psi, h - d, wh, ySection, HeightSection, remaining, indexes, result, newAreas, barriers, newRectangles, prevBarriers);
                    newAreas.add(new Types.Area(x + psi, y, w - psi, h));
                    barriers.add(new Types.Barrier(x + psi, (x + psi) + (w - psi), y));
                    lastBarrier = barriers.size() - 1;
                    barriers = barriersCheck(barriers, prevBarriers, lastBarrier, ySection, newRectangles.get(lastRectangle));
                    recursive_packing(x + psi, y, w - psi, h, wh, ySection, HeightSection, remaining, indexes, result, newAreas, barriers, newRectangles, prevBarriers);
                }
            }

        }
    }

    ArrayList<Types.Barrier> barriersCheck(ArrayList<Types.Barrier> barriers, ArrayList<Types.Barrier> prevBarriers,
                                           int lastBarrier, long ySection, Types.Rectangle lastRectanle) {

        boolean crossingWithRectangle = false;
        int crossingWithNewRectangles = 0;

        Types.Barrier intermediateBarrier = new Types.Barrier(
                barriers.get(lastBarrier).getX1(),
                barriers.get(lastBarrier).getX2(),
                barriers.get(lastBarrier).getY()
        );

        //связка с барьерами других секций
        if (prevBarriers != null) {
            for (Types.Barrier prevBarrier : prevBarriers) {
                if (barriers.get(lastBarrier).getX1() > prevBarrier.getX1() &&
                        barriers.get(lastBarrier).getX2() < prevBarrier.getX2() &&
                        barriers.get(lastBarrier).getY() == prevBarrier.getY()) {
                    break;
                }
                if (barriers.get(lastBarrier).getX1() <= prevBarrier.getX2() &&
                        barriers.get(lastBarrier).getX2() >= prevBarrier.getX2() &&
                        barriers.get(lastBarrier).getY() == prevBarrier.getY()) {
                    barriers.get(lastBarrier).setX2(prevBarrier.getX2());
                }

                //проверка на то, равен ли барьер верхней границе прямоугольника из другой секции
                //если да, то барьер не может быть удален, так как как с него может начинаться область при условии,
                //что на данный барьер не был поставлен прямоугольник
//                if (barriers.get(lastBarrier).getX1() == prevBarrier.getX1() &&
//                        barriers.get(lastBarrier).getX2() == prevBarrier.getX2() &&
//                        barriers.get(lastBarrier).getY() == prevBarrier.getY()) {
//                    crossingWithRectangle = true;
//                }
            }

            //отсечение барьера, если он не лежит на прямоугольнике из другой секции
            if (intermediateBarrier.getX1() == barriers.get(lastBarrier).getX1() &&
                    intermediateBarrier.getX2() == barriers.get(lastBarrier).getX2() &&
                    intermediateBarrier.getY() == barriers.get(lastBarrier).getY() &&
                    ySection == barriers.get(lastBarrier).getY()
                    ) {
                barriers.get(lastBarrier).setX1(0);
                barriers.get(lastBarrier).setX2(0);
                barriers.get(lastBarrier).setY(0);
            }
        }

        return barriers;
    }

    double efficiencyRatio(long width, long height, ArrayList<Types.Rectangle> rectangles) {

        double tapeArea;
        double wasteArea;
        double rectanglesAreas = 0;

        for (Types.Rectangle rectangle : rectangles) {
            rectanglesAreas = rectanglesAreas + (rectangle.getW() * rectangle.getH());
        }

        tapeArea = (double) width * height;
        wasteArea = tapeArea - rectanglesAreas;

        System.out.println("Площадь отходов = " + wasteArea);
        return ((wasteArea * 100) / tapeArea);
    }
}