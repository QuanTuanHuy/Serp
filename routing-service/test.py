from services.routing_service import process_routing_request

input = """
{
  "depot": {
    "lat": 0.12999999523162842,
    "lng": 31.100000381469727,
    "depot_id": "FAC4CF1DEC7484B4CA9A2DC8982948C3028"
  },
  "vehicles": [
    {
      "vehicle_id": "VSP4F5C518E08644D9786A762DFEB9423EE",
      "max_weight": 2000,
      "max_volume": 9
    }
  ],
  "slips": [
    {
      "lat": 9,
      "lng": 8.1,
      "weight": 5,
      "volume": 0,
      "slip_id": "DLS7932A855F8C0471EA58C89D19817B786"
    },
    {
      "lat": 9,
      "lng": 8.1,
      "weight": 5,
      "volume": 0,
      "slip_id": "DLSF18E9038207A4C31A0C8A471580DA116"
    }
  ],
  "plan_id": "DLP731D1185E72C49389EC570467858F6A7"
}
"""
input_dict = eval(input)
output = process_routing_request(input_dict)
print(output)

