package util;

import entities.Component;

public class TestHibernate {

    public static void main(String[] args) {
        System.out.println("Hello World!");

        try (var s = HibernateUtil.getSessionFactory().openSession()) {
            s.beginTransaction();

            var component = new Component();
            s.persist(component);

            var comp2 = new Component();
            s.persist(comp2);

            var comp3 = new Component();
            s.persist(comp3);

            s.getTransaction().commit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
