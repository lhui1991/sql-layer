SELECT customers.name,order_date,sku,quan
FROM customers,orders,items
WHERE customers.cid = orders.cid
AND orders.oid = items.oid
AND customers.name = 'Jones'
AND items.sku > '5000'
