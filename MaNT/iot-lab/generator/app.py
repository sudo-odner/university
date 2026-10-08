import json
import os
import random
import time
from datetime import datetime, timezone

import paho.mqtt.client as mqtt

host = os.getenv("MQTT_HOST", "mosquitto")
port = int(os.getenv("MQTT_PORT", "1883"))
device_id = os.getenv("DEVICE_ID", "student-00")
interval = float(os.getenv("INTERVAL", "5"))

telemetry_topic = f"iot/{device_id}/telemetry"
command_topic = f"iot/{device_id}/commands"
state_topic = f"iot/{device_id}/state"
status_topic = f"iot/{device_id}/status"

fan_state = "OFF"


def now():
    return datetime.now(timezone.utc).isoformat()


def publish_state(client):
    message = {"deviceId": device_id, "fan": fan_state, "timestamp": now()}
    client.publish(state_topic, json.dumps(message), qos=1, retain=True)


def on_connect(client, userdata, flags, reason_code, properties):
    if reason_code.is_failure:
        print(f"Ошибка подключения: {reason_code}")
        return

    print("Подключено к MQTT Broker")
    client.subscribe(command_topic, qos=1)
    client.publish(
        status_topic, payload=json.dumps({"status": "online"}), qos=1, retain=True
    )
    publish_state(client)


def on_message(client, userdata, msg):
    global fan_state

    try:
        data = json.loads(msg.payload.decode("utf-8"))
    except Exception:
        print("Получена некорректная команда")
        return

    if data.get("command") != "setFan":
        return

    value = data.get("value")
    if value is True:
        fan_state = "ON"

    elif value is False:
        fan_state = "OFF"
    else:

        print("Некорректное значение команды")
        return

    print(f"Команда выполнена. fan = {fan_state}")
    publish_state(client)


client = mqtt.Client(
    mqtt.CallbackAPIVersion.VERSION2, client_id=f"{device_id}-simulator"
)

client.will_set(
    status_topic, payload=json.dumps({"status": "ffline"}), qos=1, retain=True
)
client.on_connect = on_connect
client.on_message = on_message

while True:
    try:
        client.connect(host, port, 60)
        break
    except OSError:
        print("Broker недоступен, повтор через 2 секунды...")
        time.sleep(2)

client.loop_start()

while True:
    message = {
        "deviceId": device_id,
        "value": round(random.uniform(20.0, 30.0), 1),
        "timestamp": datetime.now(timezone.utc).isoformat(),
    }
    payload = json.dumps(message)

    client.publish(telemetry_topic, payload)
    print(f"{telemetry_topic}: {payload}")
    time.sleep(interval)
